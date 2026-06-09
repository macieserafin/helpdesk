import { getActiveCategories } from '../../api/categoryApi.js';
import { getUserDashboard } from '../../api/userApi.js';
import { PageHeader } from '../../components/common/PageHeader.js';
import { PriorityBadge, StatusBadge } from '../../components/common/Badges.js';
import { formatDateTime } from '../../utils/dateFormatter.js';
import { escapeHtml, htmlToElement } from '../../utils/dom.js';
import { displayUserName } from '../../utils/userDisplay.js';

const TRACKED_STATUSES = ['OPEN', 'IN_PROGRESS', 'WAITING_FOR_USER', 'RESOLVED', 'CLOSED'];

export async function UserDashboardPage({ navigate, user }) {
  const [dashboard, categories] = await Promise.all([
    getUserDashboard(),
    getActiveCategories()
  ]);

  const page = htmlToElement(`
    <section class="page stack user-dashboard">
      <div data-header></div>
      <div class="action-strip">
        <button class="button button-primary" type="button" data-route="/user/tickets/new">Nowe zgłoszenie</button>
        <button class="button button-secondary" type="button" data-route="/user/tickets">Moje tickety</button>
        <button class="button button-ghost" type="button" data-route="/user/profile">Profil</button>
      </div>

      ${dashboard.totalTickets === 0 ? renderEmptyDashboard() : renderMetrics(dashboard)}

      <section class="dashboard-grid">
        <div class="stack">
          ${dashboard.totalTickets === 0 ? '' : renderActionRequired(dashboard.requiresUserAction)}
          ${renderLatestTickets(dashboard.latestTickets)}
          ${renderRecentActivity(dashboard.recentActivity)}
        </div>
        <aside class="stack">
          ${renderStatusBreakdown(dashboard.statusBreakdown)}
          ${renderQuickHelp(categories)}
        </aside>
      </section>
    </section>
  `);

  page.querySelector('[data-header]').replaceWith(PageHeader({
    eyebrow: 'Panel użytkownika',
    title: `Witaj, ${displayUserName(user)}`,
    description: 'Zgłaszaj problemy, śledź status i odpowiadaj supportowi w jednym miejscu.'
  }));

  page.querySelectorAll('[data-route]').forEach((button) => {
    button.addEventListener('click', () => navigate(button.dataset.route));
  });
  page.querySelectorAll('[data-ticket-id]').forEach((element) => {
    const openTicket = () => navigate(`/tickets/${element.dataset.ticketId}`);
    element.addEventListener('click', openTicket);

    if (element.tagName !== 'BUTTON') {
      element.addEventListener('keydown', (event) => {
        if (event.key === 'Enter' || event.key === ' ') {
          event.preventDefault();
          openTicket();
        }
      });
    }
  });
  page.querySelectorAll('[data-new-category]').forEach((button) => {
    button.addEventListener('click', () => navigate('/user/tickets/new'));
  });

  return page;
}

function renderMetrics(dashboard) {
  return `
    <div class="metric-grid user-metric-grid">
      ${renderMetric('Wszystkie moje tickety', dashboard.totalTickets)}
      ${renderMetric('Otwarte', dashboard.openTickets)}
      ${renderMetric('Czekają na support', dashboard.waitingForSupport)}
      ${renderMetric('Czekają na moją odpowiedź', dashboard.waitingForUser)}
      ${renderMetric('Rozwiązane', dashboard.resolvedTickets)}
      ${renderMetric('Zamknięte', dashboard.closedTickets)}
    </div>
  `;
}

function renderMetric(label, value, valueClass = '') {
  return `
    <article class="metric-card">
      <span>${escapeHtml(label)}</span>
      <strong class="${valueClass}">${escapeHtml(value)}</strong>
    </article>
  `;
}

function renderEmptyDashboard() {
  return `
    <section class="empty-state empty-dashboard">
      <h2>Nie masz jeszcze zgłoszeń</h2>
      <p>Utwórz pierwsze zgłoszenie, a tutaj pojawią się statusy, komentarze i ostatnia aktywność.</p>
      <button class="button button-primary" type="button" data-route="/user/tickets/new">Utwórz pierwsze zgłoszenie</button>
    </section>
  `;
}

function renderActionRequired(tickets = []) {
  return `
    <section class="card stack">
      <div class="section-title">
        <h2>Wymagają mojej reakcji</h2>
        <span class="count-pill">${tickets.length}</span>
      </div>
      ${tickets.length ? renderTicketCards(tickets) : `
        <div class="quiet-panel">
          <strong>Brak spraw do reakcji</strong>
          <p>Gdy support poprosi o doprecyzowanie albo oznaczy sprawę jako rozwiązaną, zobaczysz ją tutaj.</p>
        </div>
      `}
    </section>
  `;
}

function renderLatestTickets(tickets = []) {
  return `
    <section class="card stack">
      <div class="section-title">
        <h2>Ostatnie zgłoszenia</h2>
        <button class="button button-secondary button-small" type="button" data-route="/user/tickets">Pokaż wszystkie</button>
      </div>
      ${tickets.length ? renderTicketCards(tickets) : `
        <div class="quiet-panel">
          <strong>Lista jest pusta</strong>
          <p>Nowe zgłoszenia pojawią się tutaj po utworzeniu pierwszej sprawy.</p>
        </div>
      `}
    </section>
  `;
}

function renderTicketCards(tickets) {
  return `
    <div class="ticket-row-list">
      ${tickets.map((ticket) => `
        <article class="ticket-summary-row ticket-open-row" data-ticket-id="${ticket.id}" role="button" tabindex="0">
          <div class="ticket-summary-main">
            <span class="ticket-id">#${ticket.id}</span>
            <h3>${escapeHtml(ticket.title)}</h3>
            <div class="badge-row">
              ${StatusBadge(ticket.status)}
              ${PriorityBadge(ticket.priority)}
              <span class="badge category-badge">${escapeHtml(ticket.category)}</span>
            </div>
          </div>
          <div class="ticket-summary-meta">
            <span>Aktualizacja</span>
            <strong>${formatDateTime(ticket.updatedAt || ticket.createdAt)}</strong>
          </div>
        </article>
      `).join('')}
    </div>
  `;
}

function renderStatusBreakdown(statusBreakdown = {}) {
  const maxCount = Math.max(...TRACKED_STATUSES.map((status) => statusBreakdown[status] || 0), 1);

  return `
    <section class="card stack">
      <h2>Status moich spraw</h2>
      <div class="status-breakdown">
        ${TRACKED_STATUSES.map((status) => {
          const count = statusBreakdown[status] || 0;
          const width = Math.max(6, Math.round((count / maxCount) * 100));
          return `
            <div class="status-row">
              <div>
                ${StatusBadge(status)}
                <strong>${count}</strong>
              </div>
              <span class="status-bar"><span style="width: ${width}%"></span></span>
            </div>
          `;
        }).join('')}
      </div>
    </section>
  `;
}

function renderRecentActivity(activity = []) {
  return `
    <section class="card stack">
      <div class="section-title">
        <h2>Ostatnia aktywność</h2>
        <span class="count-pill">${activity.length}</span>
      </div>
      ${activity.length ? `
        <div class="activity-list">
          ${activity.map((item) => `
            <article class="activity-item">
              <div class="activity-marker activity-${String(item.type || 'history').toLowerCase()}"></div>
              <div>
                <div class="activity-heading">
                  <button class="link-button" type="button" data-ticket-id="${item.ticketId}">
                    #${item.ticketId} ${escapeHtml(item.ticketTitle)}
                  </button>
                  <span>${activityTypeLabel(item.type)}</span>
                </div>
                <p>${escapeHtml(item.description)}</p>
                <small>${escapeHtml(item.actor)} · ${formatDateTime(item.occurredAt)}</small>
              </div>
            </article>
          `).join('')}
        </div>
      ` : `
        <div class="quiet-panel">
          <strong>Brak aktywności</strong>
          <p>Historia statusów, komentarze i załączniki pojawią się po rozpoczęciu obsługi zgłoszenia.</p>
        </div>
      `}
    </section>
  `;
}

function renderQuickHelp(categories = []) {
  return `
    <section class="card stack">
      <div class="section-title">
        <h2>Szybka pomoc</h2>
        <button class="button button-primary button-small" type="button" data-route="/user/tickets/new">Nowe zgłoszenie</button>
      </div>
      ${categories.length ? `
        <div class="quick-help-list">
          ${categories.slice(0, 6).map((category) => `
            <button class="quick-help-item" type="button" data-new-category="${escapeHtml(category.name)}">
              <strong>${escapeHtml(category.name)}</strong>
              <span>${escapeHtml(category.description || 'Utwórz zgłoszenie w tej kategorii')}</span>
            </button>
          `).join('')}
        </div>
      ` : `
        <div class="quiet-panel">
          <strong>Brak aktywnych kategorii</strong>
          <p>Skontaktuj się z administratorem, jeśli nie możesz utworzyć zgłoszenia.</p>
        </div>
      `}
    </section>
  `;
}

function activityTypeLabel(type) {
  const labels = {
    COMMENT: 'Komentarz',
    HISTORY: 'Status',
    ATTACHMENT: 'Załącznik'
  };

  return labels[type] || 'Aktywność';
}
