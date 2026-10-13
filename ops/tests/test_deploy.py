"""Exercise release state and rollback without contacting Docker or a server."""
import json
import os
from pathlib import Path
import shutil
import subprocess
import tempfile
import unittest


NEW_SHA = "a" * 40
OLD_SHA = "b" * 40
SCRIPT = Path(__file__).resolve().parents[1] / "deploy.sh"


class DeploymentTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name).resolve() / "app"
        self.release = self.root / "releases" / NEW_SHA
        (self.release / "ops").mkdir(parents=True)
        shutil.copyfile(SCRIPT, self.release / "ops" / "deploy.sh")
        (self.release / "compose.prod.yml").write_text("services: {}\n")
        (self.root / ".env").write_text("POSTGRES_PASSWORD=test-only\n")
        self.bin = Path(self.temp.name) / "bin"
        self.bin.mkdir()
        self.log = Path(self.temp.name) / "commands.jsonl"
        self.env = dict(os.environ, PATH=f"{self.bin}:{os.environ['PATH']}",
                        COMMAND_LOG=str(self.log), NEW_SHA=NEW_SHA)
        self.command("sudo", """#!/usr/bin/env bash
while [[ "${1:-}" == -* ]]; do shift; done
exec "$@"
""")
        self.command("flock", "#!/usr/bin/env bash\nexit 0\n")
        self.command("curl", "#!/usr/bin/env bash\nexit 0\n")
        self.command("docker", """#!/usr/bin/env python3
import base64, json, os, pathlib, sys
args = sys.argv[1:]
with open(os.environ['COMMAND_LOG'], 'a') as log:
    log.write(json.dumps({'args': args, 'tag': os.getenv('IMAGE_TAG')}) + '\\n')
if 'login' in args:
    sys.exit('Simulated Docker Snap credential write permission denied')
if 'pull' in args:
    auth_dir = pathlib.Path(args[args.index('--config') + 1])
    config = json.loads((auth_dir / 'config.json').read_text())
    assert base64.b64decode(config['auths']['ghcr.io']['auth']) == b'test-user:temporary-token'
    assert (auth_dir / 'config.json').stat().st_mode & 0o777 == 0o600
if 'pull' in args and os.getenv('FAIL_PULL'):
    sys.exit(1)
if 'up' in args and os.getenv('FAIL_UP') and os.getenv('IMAGE_TAG') == os.environ['NEW_SHA']:
    sys.exit(1)
""")

    def command(self, name, content):
        path = self.bin / name
        path.write_text(content)
        path.chmod(0o755)

    def previous(self):
        previous = self.root / "releases" / OLD_SHA
        previous.mkdir()
        (previous / "compose.prod.yml").write_text("services: {}\n")
        (previous / "image-namespace").write_text("example-owner\n")
        (previous / "release.env").write_text(f"IMAGE_TAG={OLD_SHA}\nIMAGE_NAMESPACE=example-owner\nAPP_PORT=3001\n")
        (self.root / "current").symlink_to(previous)
        return previous

    def run_deploy(self, **env):
        result = subprocess.run(
            ["bash", str(self.release / "ops" / "deploy.sh"), NEW_SHA, "test-user", "example-owner"],
            input="temporary-token", text=True, capture_output=True,
            env=dict(self.env, **env), timeout=10,
        )
        self.assertEqual(list(self.root.glob("docker-auth.*")), [])
        return result

    def commands(self):
        return [json.loads(line) for line in self.log.read_text().splitlines()]

    def test_success_sets_current_release(self):
        result = self.run_deploy()
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertEqual((self.root / "current").resolve(), self.release)

    def test_failed_start_restores_previous_release_and_reports_failure(self):
        previous = self.previous()
        result = self.run_deploy(FAIL_UP="1")
        self.assertNotEqual(result.returncode, 0)
        self.assertEqual((self.root / "current").resolve(), previous)
        starts = [call for call in self.commands() if "up" in call["args"]]
        self.assertEqual([call["tag"] for call in starts], [NEW_SHA, OLD_SHA])

    def test_failed_pull_does_not_replace_running_release(self):
        previous = self.previous()
        result = self.run_deploy(FAIL_PULL="1")
        self.assertNotEqual(result.returncode, 0)
        self.assertEqual((self.root / "current").resolve(), previous)
        self.assertFalse(any("up" in call["args"] for call in self.commands()))

    def test_first_deployment_failure_does_not_mark_release_current(self):
        result = self.run_deploy(FAIL_UP="1")
        self.assertNotEqual(result.returncode, 0)
        self.assertFalse((self.root / "current").exists())

    def test_missing_environment_prevents_docker_commands(self):
        (self.root / ".env").unlink()
        result = self.run_deploy()
        self.assertNotEqual(result.returncode, 0)
        self.assertFalse(self.log.exists())


if __name__ == "__main__":
    unittest.main()
