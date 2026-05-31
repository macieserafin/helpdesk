import { getMyTickets } from '../../api/ticketApi.js';
import { PageHeader } from '../../components/common/PageHeader.js';
import { StatusBadge } from '../../components/common/Badges.js';
import { TicketTable } from '../../components/tickets/TicketTable.js';
import { htmlToElement } from '../../utils/dom.js';
import { pageContent, pageMeta } from '../../utils/pageResponse.js';
import { displayUserName } from '../../utils/userDisplay.js';

export async function UserDashboardPage({ navigate, user }) {
  const response = await getMyTickets({ page: 0, size: 100 });
  const tickets = pageContent(response);
  const meta = pageMeta(response);
  const open = tickets.filter((ticket) => !['CLOSED', 'RESOLVED'].includes(ticket.status)).length;
  const closed = tickets.filter((ticket) => ticket.status === 'CLOSED').length;
  const latest = tickets.slice(0, 5);

  const page = htmlToElement(`
    <section class="page stack">
      <div data-header></div>
      <div class="metric-grid">
        <article class="metric-card"><span>Wszystkie</span><strong>${meta.totalElements}</strong></article>
        <article class="metric-card"><span>Aktywne</span><strong>${open}</strong></article>
        <article class="metric-card"><span>Zamkniete</span><strong>${closed}</strong></article>
        <article class="metric-card"><span>Profil</span><strong>${user.profile?.city || 'Uzupelnij'}</strong></article>
      </div>
      <section class="card stack">
        <div class="section-title"><h2>Ostatnie zgloszenia</h2><button class="button button-secondary" data-new>Nowy ticket</button></div>
        <div data-table></div>
      </section>
      <section class="card">
        <h2>Statusy w toku</h2>
        <div class="badge-row">${tickets.slice(0, 8).map((ticket) => StatusBadge(ticket.status)).join('')}</div>
      </section>
    </section>
  `);

  page.querySelector('[data-header]').replaceWith(PageHeader({
    eyebrow: 'Panel uzytkownika',
    title: `Witaj, ${displayUserName(user)}`,
    description: 'Tworz i monitoruj swoje zgloszenia serwisowe.'
  }));
  page.querySelector('[data-table]').replaceWith(TicketTable({ tickets: latest }));
  page.querySelector('[data-new]').addEventListener('click', () => navigate('/user/tickets/new'));

  return page;
}
