import { getMyTickets } from '../../api/ticketApi.js';
import { PageHeader } from '../../components/common/PageHeader.js';
import { TicketTable } from '../../components/tickets/TicketTable.js';
import { htmlToElement } from '../../utils/dom.js';

export async function MyTicketsPage() {
  const tickets = await getMyTickets();
  const page = htmlToElement('<section class="page stack"><div data-header></div><div data-table></div></section>');
  page.querySelector('[data-header]').replaceWith(PageHeader({
    eyebrow: 'USER',
    title: 'Moje tickety',
    description: 'Lista zgloszen utworzonych przez aktualnie zalogowanego uzytkownika.'
  }));
  page.querySelector('[data-table]').replaceWith(TicketTable({ tickets }));
  return page;
}
