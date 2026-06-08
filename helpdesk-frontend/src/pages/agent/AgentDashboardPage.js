import * as agentApi from '../../api/agentApi.js';
import { PageHeader } from '../../components/common/PageHeader.js';
import { PriorityBadge, StatusBadge } from '../../components/common/Badges.js';
import { PRIORITY_LABELS, STATUS_LABELS } from '../../utils/constants.js';
import { formatDateTime } from '../../utils/dateFormatter.js';
import { escapeHtml, htmlToElement } from '../../utils/dom.js';
import { getErrorMessage } from '../../utils/errorMessage.js';
import { displayUserName } from '../../utils/userDisplay.js';

const STATUS_ACTIONS = {
  OPEN: ['IN_PROGRESS', 'REJECTED', 'CANCELLED'],
  IN_PROGRESS: ['WAITING_FOR_USER', 'RESOLVED', 'REJECTED', 'CANCELLED'],
  WAITING_FOR_USER: ['IN_PROGRESS', 'RESOLVED', 'CANCELLED']
};

export async function AgentDashboardPage({ navigate, user, showToast }) {
  const page = htmlToElement('<section class="page stack agent-dashboard"><div data-content></div></section>');
  const priorities = await agentApi.getAssignableTicketPriorities();

  async function load() {
    const dashboard = await agentApi.getAgentDashboard();
    const content = htmlToElement(`
      <div class="stack">
        <div data-header></div>
        <div class="action-strip">
          <button class="button button-primary" type="button" data-route="/agent/assigned">Moje zgłoszenia</button>
          <button class="button button-secondary" type="button" data-route="/agent/tickets">Wszystkie zgłoszenia</button>
          <button class="button button-ghost" type="button" data-route="/user/profile">Profil</button>
        </div>
        ${renderMetrics(dashboard)}
        <section class="dashboard-grid">
          <div class="stack">
            ${renderWorkQueue(dashboard.myQueue, priorities)}
            ${renderTakeoverQueue(dashboard.takeoverQueue)}
            ${renderCustomerReplied(dashboard.customerRepliedTickets)}
          </div>
          <aside class="stack">
            ${renderHighPriority(dashboard.highPriorityTickets)}
            ${renderResolvedToday(dashboard.resolvedTodayTickets)}
            ${renderSavedViews(dashboard)}
          </aside>
        </section>
      </div>
    `);

    content.querySelector('[data-header]').replaceWith(PageHeader({
      eyebrow: 'Panel agenta',
      title: `Centrum pracy: ${displayUserName(user)}`,
      description: 'Priorytetyzuj zgłoszenia, przejmuj sprawy i reaguj na odpowiedzi użytkowników.'
    }));

    bindDashboardEvents(content);
    page.querySelector('[data-content]').replaceChildren(...content.childNodes);
  }

  function bindDashboardEvents(root) {
    root.querySelectorAll('[data-route]').forEach((button) => {
      button.addEventListener('click', () => navigate(button.dataset.route));
    });

    root.querySelectorAll('[data-ticket-id]').forEach((button) => {
      button.addEventListener('click', () => navigate(`/tickets/${button.dataset.ticketId}`));
    });

    root.querySelectorAll('[data-comment-id]').forEach((button) => {
      button.addEventListener('click', () => navigate(`/tickets/${button.dataset.commentId}`));
    });

    root.querySelectorAll('[data-assign-id]').forEach((button) => {
      button.addEventListener('click', async () => {
        const originalText = button.textContent;
        button.disabled = true;
        button.textContent = 'Przypisuję...';
        try {
          await agentApi.assignTicket(button.dataset.assignId);
          showToast('Ticket został przypisany do Ciebie.', 'success');
          await load();
        } catch (error) {
          showToast(getErrorMessage(error), 'error');
          button.disabled = false;
          button.textContent = originalText;
        }
      });
    });

    root.querySelectorAll('[data-status-form]').forEach((form) => {
      form.addEventListener('submit', async (event) => {
        event.preventDefault();
        const submit = form.querySelector('[type="submit"]');
        const originalText = submit.textContent;
        submit.disabled = true;
        submit.textContent = 'Zmieniam...';
        try {
          await agentApi.updateTicketStatus(form.dataset.statusForm, new FormData(form).get('status'));
          showToast('Status został zmieniony.', 'success');
          await load();
        } catch (error) {
          showToast(getErrorMessage(error), 'error');
          submit.disabled = false;
          submit.textContent = originalText;
        }
      });
    });

    root.querySelectorAll('[data-priority-form]').forEach((form) => {
      form.addEventListener('submit', async (event) => {
        event.preventDefault();
        const submit = form.querySelector('[type="submit"]');
        const originalText = submit.textContent;
        submit.disabled = true;
        submit.textContent = 'Zmieniam...';
        try {
          await agentApi.updateTicketPriority(form.dataset.priorityForm, new FormData(form).get('priority'));
          showToast('Priorytet został zmieniony.', 'success');
          await load();
        } catch (error) {
          showToast(getErrorMessage(error), 'error');
          submit.disabled = false;
          submit.textContent = originalText;
        }
      });
    });
  }

  await load();
  return page;
}

function renderMetrics(dashboard) {
  return `
    <div class="metric-grid agent-metric-grid">
      ${renderMetric('Moje aktywne', dashboard.assignedActive)}
      ${renderMetric('Do przejęcia', dashboard.unassignedOpen)}
      ${renderMetric('Czeka na mnie', dashboard.waitingForAgent)}
      ${renderMetric('Czeka na użytkownika', dashboard.waitingForUser)}
      ${renderMetric('Klient odpowiedział', dashboard.customerReplied, dashboard.customerReplied ? 'metric-alert' : '')}
      ${renderMetric('Zakończone dzisiaj', dashboard.resolvedToday)}
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

function renderWorkQueue(tickets = [], priorities = []) {
  return renderQueueSection({
    title: 'Moje zgłoszenia',
    count: tickets.length,
    emptyTitle: 'Brak aktywnych ticketów',
    emptyText: 'Przejmij zgłoszenie z listy wszystkich zgłoszeń lub poczekaj na nową odpowiedź klienta.',
    body: renderTicketCards(tickets, { mode: 'work', priorities })
  });
}

function renderTakeoverQueue(tickets = []) {
  return renderQueueSection({
    title: 'Do przejęcia',
    count: tickets.length,
    emptyTitle: 'Nie ma ticketów do przejęcia',
    emptyText: 'Otwarte zgłoszenia bez agenta pojawią się tutaj, z priorytetem dla HIGH i CRITICAL.',
    body: renderTicketCards(tickets, { mode: 'takeover' })
  });
}

function renderCustomerReplied(tickets = []) {
  return renderQueueSection({
    title: 'Klient odpowiedział',
    count: tickets.length,
    emptyTitle: 'Brak odpowiedzi oczekujących na reakcję',
    emptyText: 'Gdy użytkownik odpowie w przypisanym tickecie, zobaczysz go w tej sekcji.',
    body: renderTicketCards(tickets, { mode: 'customer' })
  });
}

function renderHighPriority(tickets = []) {
  return renderQueueSection({
    title: 'Wysokie priorytety',
    count: tickets.length,
    emptyTitle: 'Brak aktywnych wysokich priorytetów',
    emptyText: 'Tickety HIGH i CRITICAL pojawią się tutaj niezależnie od przypisania.',
    compact: true,
    body: renderCompactTicketList(tickets, 'Otwórz')
  });
}

function renderResolvedToday(tickets = []) {
  return renderQueueSection({
    title: 'Zakończone zgłoszenia',
    count: tickets.length,
    emptyTitle: 'Jeszcze nic nie rozwiązano dzisiaj',
    emptyText: 'Rozwiązane dzisiaj tickety przypisane do Ciebie będą widoczne tutaj.',
    compact: true,
    body: renderCompactTicketList(tickets, 'Zobacz')
  });
}

function renderSavedViews(dashboard) {
  const views = [
    ['Moje zgłoszenia', dashboard.assignedActive, '/agent/assigned'],
    ['Nieprzypisane', dashboard.unassignedOpen, '/agent/tickets'],
    ['Klient odpowiedział', dashboard.customerReplied, '/agent'],
    ['Zakończone dzisiaj', dashboard.resolvedToday, '/agent']
  ];

  return `
    <section class="card stack">
      <div class="section-title">
        <h2>Zapisane widoki</h2>
      </div>
      <div class="saved-view-list">
        ${views.map(([label, count, route]) => `
          <button class="saved-view-button" type="button" data-route="${route}">
            <span>${escapeHtml(label)}</span>
            <strong>${escapeHtml(count)}</strong>
          </button>
        `).join('')}
      </div>
      <small class="agent-last-update">Ostatnia aktywność: ${formatDateTime(dashboard.lastUpdatedAt)}</small>
    </section>
  `;
}

function renderQueueSection({ title, count, body, emptyTitle, emptyText, compact = false }) {
  return `
    <section class="card stack ${compact ? 'agent-compact-panel' : ''}">
      <div class="section-title">
        <h2>${escapeHtml(title)}</h2>
        <span class="count-pill">${count}</span>
      </div>
      ${count ? body : `
        <div class="quiet-panel">
          <strong>${escapeHtml(emptyTitle)}</strong>
          <p>${escapeHtml(emptyText)}</p>
        </div>
      `}
    </section>
  `;
}

function renderTicketCards(tickets, { mode, priorities = [] }) {
  if (!tickets.length) {
    return '';
  }

  return `
    <div class="ticket-row-list">
      ${tickets.map((ticket) => `
        <article class="ticket-summary-row agent-ticket-row">
          <div class="ticket-summary-main">
            <span class="ticket-id">#${ticket.id}</span>
            <h3>${escapeHtml(ticket.title)}</h3>
            <div class="badge-row">
              ${StatusBadge(ticket.status)}
              ${PriorityBadge(ticket.priority)}
              <span class="badge category-badge">${escapeHtml(ticket.category)}</span>
            </div>
            <div class="agent-ticket-context">
              <span>Autor: ${escapeHtml(ticket.createdBy)}</span>
              <span>Agent: ${escapeHtml(ticket.assignedTo || 'Nieprzypisany')}</span>
            </div>
          </div>
          <div class="ticket-summary-meta agent-ticket-meta">
            <span>Aktualizacja</span>
            <strong>${formatDateTime(ticket.updatedAt || ticket.createdAt)}</strong>
            ${renderTicketActions(ticket, mode, priorities)}
          </div>
        </article>
      `).join('')}
    </div>
  `;
}

function renderTicketActions(ticket, mode, priorities) {
  if (mode === 'takeover') {
    return `
      <div class="row-actions agent-quick-actions">
        <button class="button button-primary button-small" type="button" data-assign-id="${ticket.id}">Przypisz do mnie</button>
        <button class="button button-ghost button-small" type="button" data-ticket-id="${ticket.id}">Szczegóły</button>
      </div>
    `;
  }

  return `
    <div class="agent-quick-actions">
      <div class="row-actions agent-ticket-buttons">
        <button class="button button-secondary button-small" type="button" data-comment-id="${ticket.id}">Komentarz</button>
        <button class="button button-ghost button-small" type="button" data-ticket-id="${ticket.id}">Szczegóły</button>
      </div>
      <div class="agent-ticket-forms">
        ${renderStatusAction(ticket)}
        ${renderPriorityAction(ticket, priorities)}
      </div>
    </div>
  `;
}

function renderStatusAction(ticket) {
  const statuses = STATUS_ACTIONS[ticket.status] || [];
  if (!statuses.length) {
    return '';
  }

  return `
    <form class="inline-form agent-inline-action" data-status-form="${ticket.id}">
      <div class="inline-form-row">
        <select name="status">
          ${statuses.map((status) => `<option value="${status}">${STATUS_LABELS[status] || status}</option>`).join('')}
        </select>
        <button class="button button-secondary button-small" type="submit">Status</button>
      </div>
    </form>
  `;
}

function renderPriorityAction(ticket, priorities = []) {
  if (!priorities.length) {
    return '';
  }

  return `
    <form class="inline-form agent-inline-action" data-priority-form="${ticket.id}">
      <div class="inline-form-row">
        <select name="priority">
          ${priorities.map((priority) => `
            <option value="${priority}" ${priority === ticket.priority ? 'selected' : ''}>${PRIORITY_LABELS[priority] || priority}</option>
          `).join('')}
        </select>
        <button class="button button-secondary button-small" type="submit">Priorytet</button>
      </div>
    </form>
  `;
}

function renderCompactTicketList(tickets = [], actionLabel) {
  if (!tickets.length) {
    return '';
  }

  return `
    <div class="agent-compact-list">
      ${tickets.map((ticket) => `
        <article class="agent-compact-ticket">
          <div>
            <span class="ticket-id">#${ticket.id}</span>
            <strong>${escapeHtml(ticket.title)}</strong>
            <div class="badge-row">
              ${StatusBadge(ticket.status)}
              ${PriorityBadge(ticket.priority)}
            </div>
          </div>
          <button class="button button-ghost button-small" type="button" data-ticket-id="${ticket.id}">
            ${escapeHtml(actionLabel)}
          </button>
        </article>
      `).join('')}
    </div>
  `;
}
