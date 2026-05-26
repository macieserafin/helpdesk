import { getTicketQueue } from '../../api/agentApi.js';
import { PageHeader } from '../../components/common/PageHeader.js';
import { TicketTable } from '../../components/tickets/TicketTable.js';
import { htmlToElement } from '../../utils/dom.js';

export async function AgentDashboardPage({ user }) {
  const tickets = await getTicketQueue();
  const assigned = tickets.filter((ticket) => ticket.assignedTo === user.username);
  const waiting = tickets.filter((ticket) => !ticket.assignedTo && ticket.status === 'OPEN');
  const urgent = tickets.filter((ticket) => ['HIGH', 'CRITICAL'].includes(ticket.priority));

  const page = htmlToElement(`
    <section class="page stack">
      <div data-header></div>
      <div class="metric-grid">
        <article class="metric-card"><span>Kolejka</span><strong>${tickets.length}</strong></article>
        <article class="metric-card"><span>Moje</span><strong>${assigned.length}</strong></article>
        <article class="metric-card"><span>Nieprzypisane</span><strong>${waiting.length}</strong></article>
        <article class="metric-card"><span>Wysoki priorytet</span><strong>${urgent.length}</strong></article>
      </div>
      <section class="card stack">
        <div class="section-title"><h2>Najpilniejsze</h2></div>
        <div data-table></div>
      </section>
    </section>
  `);

  page.querySelector('[data-header]').replaceWith(PageHeader({
    eyebrow: 'Panel agenta',
    title: `Kolejka pracy: ${user.username}`,
    description: 'Przypisuj tickety, aktualizuj statusy i komentuj przebieg obslugi.'
  }));
  page.querySelector('[data-table]').replaceWith(TicketTable({ tickets: urgent.slice(0, 6) }));
  return page;
}
