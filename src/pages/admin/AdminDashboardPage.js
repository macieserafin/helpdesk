import { getTickets, getUsers } from '../../api/adminApi.js';
import { PageHeader } from '../../components/common/PageHeader.js';
import { TicketTable } from '../../components/tickets/TicketTable.js';
import { htmlToElement } from '../../utils/dom.js';

export async function AdminDashboardPage() {
  const [users, tickets] = await Promise.all([getUsers(), getTickets()]);
  const activeUsers = users.filter((user) => user.enabled).length;
  const activeTickets = tickets.filter((ticket) => !['CLOSED', 'CANCELLED', 'REJECTED'].includes(ticket.status)).length;

  const page = htmlToElement(`
    <section class="page stack">
      <div data-header></div>
      <div class="metric-grid">
        <article class="metric-card"><span>Uzytkownicy</span><strong>${users.length}</strong></article>
        <article class="metric-card"><span>Aktywni</span><strong>${activeUsers}</strong></article>
        <article class="metric-card"><span>Tickety</span><strong>${tickets.length}</strong></article>
        <article class="metric-card"><span>Otwarte procesy</span><strong>${activeTickets}</strong></article>
      </div>
      <section class="card stack">
        <div class="section-title"><h2>Najnowsze tickety</h2></div>
        <div data-table></div>
      </section>
    </section>
  `);

  page.querySelector('[data-header]').replaceWith(PageHeader({
    eyebrow: 'ADMIN',
    title: 'Centrum administracyjne',
    description: 'Zarzadzanie uzytkownikami i globalnym ruchem ticketow.'
  }));
  page.querySelector('[data-table]').replaceWith(TicketTable({ tickets: tickets.slice(0, 6) }));
  return page;
}
