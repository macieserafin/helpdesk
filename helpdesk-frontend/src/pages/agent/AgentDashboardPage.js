import * as agentApi from '../../api/agentApi.js';
import { PageHeader } from '../../components/common/PageHeader.js';
import { PriorityBadge, StatusBadge } from '../../components/common/Badges.js';
import { escapeHtml, htmlToElement } from '../../utils/dom.js';
import { displayUserName } from '../../utils/userDisplay.js';

export async function AgentDashboardPage({ navigate, user }) {
  const page = htmlToElement('<section class="page agent-dashboard"><div class="stack" data-content></div></section>');

  async function load() {
    const dashboard = await agentApi.getAgentDashboard();
    const content = htmlToElement(`
      <div class="stack">
        <div data-header></div>
        <div class="action-strip">
          <button class="button button-primary" type="button" data-route="/agent/assigned">Przypisane do mnie</button>
          <button class="button button-secondary" type="button" data-route="/agent/tickets">Wszystkie zgłoszenia</button>
          <button class="button button-ghost" type="button" data-route="/user/profile">Profil</button>
        </div>
        ${renderMetrics(dashboard)}
        <section class="dashboard-grid">
          <div class="stack">
            ${renderWorkQueue(dashboard.myQueue)}
            ${renderTakeoverQueue(dashboard.takeoverQueue)}
            ${renderCustomerReplied(dashboard.customerRepliedTickets)}
          </div>
          <aside class="stack">
            ${renderHighPriority(dashboard.highPriorityTickets)}
            ${renderResolvedToday(dashboard.resolvedTodayTickets)}
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

    root.querySelectorAll('[data-ticket-id]').forEach((element) => {
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

function renderWorkQueue(tickets = []) {
  return renderQueueSection({
    title: 'Przypisane do mnie',
    count: tickets.length,
    emptyTitle: 'Brak aktywnych ticketów',
    emptyText: 'Przejmij zgłoszenie z listy wszystkich zgłoszeń lub poczekaj na nową odpowiedź klienta.',
    body: renderTicketCards(tickets)
  });
}

function renderTakeoverQueue(tickets = []) {
  return renderQueueSection({
    title: 'Do przejęcia',
    count: tickets.length,
    emptyTitle: 'Nie ma ticketów do przejęcia',
    emptyText: 'Otwarte zgłoszenia bez agenta pojawią się tutaj, z priorytetem dla HIGH i CRITICAL.',
    body: renderTicketCards(tickets)
  });
}

function renderCustomerReplied(tickets = []) {
  return renderQueueSection({
    title: 'Klient odpowiedział',
    count: tickets.length,
    emptyTitle: 'Brak odpowiedzi oczekujących na reakcję',
    emptyText: 'Gdy użytkownik odpowie w przypisanym tickecie, zobaczysz go w tej sekcji.',
    body: renderTicketCards(tickets)
  });
}

function renderHighPriority(tickets = []) {
  return renderQueueSection({
    title: 'Pilne bez przypisania',
    count: tickets.length,
    emptyTitle: 'Brak pilnych nieprzypisanych ticketów',
    emptyText: 'Nieprzypisane tickety HIGH i CRITICAL pojawią się tutaj.',
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

function renderTicketCards(tickets) {
  if (!tickets.length) {
    return '';
  }

  return `
    <div class="ticket-row-list">
      ${tickets.map((ticket) => `
        <article class="ticket-summary-row agent-ticket-row" data-ticket-id="${ticket.id}" role="button" tabindex="0">
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
        </article>
      `).join('')}
    </div>
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
