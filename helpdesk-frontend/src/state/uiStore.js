let activeRequestCount = 0;
const listeners = new Set();

export function subscribeUi(listener) {
  listeners.add(listener);
  listener({ loading: activeRequestCount > 0 });
  return () => listeners.delete(listener);
}

export function startLoading() {
  activeRequestCount += 1;
  notify();
}

export function stopLoading() {
  activeRequestCount = Math.max(0, activeRequestCount - 1);
  notify();
}

function notify() {
  const snapshot = { loading: activeRequestCount > 0 };
  listeners.forEach((listener) => listener(snapshot));
}
