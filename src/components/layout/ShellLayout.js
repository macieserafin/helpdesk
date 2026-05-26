import { htmlToElement } from '../../utils/dom.js';
import { Navbar } from './Navbar.js';
import { Sidebar } from './Sidebar.js';

export function ShellLayout({ user, content, activePath }) {
  const shell = htmlToElement(`
    <div class="app-shell">
      ${Sidebar({ user, activePath })}
      <div class="workspace">
        ${Navbar({ user })}
        <main class="content"></main>
      </div>
    </div>
  `);

  shell.querySelector('.content').append(content);
  return shell;
}
