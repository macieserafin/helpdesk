import { getTicketQueue } from '../../api/agentApi.js';
import { PageHeader } from '../../components/common/PageHeader.js';
import { TicketTable } from '../../components/tickets/TicketTable.js';
import { htmlToElement } from '../../utils/dom.js';

export async function AssignedTicketsPage({ user }) {
  const tickets = (await getTicketQueue()).filter((ticket) => ticket.assignedTo === user.username);
  const page = htmlToElement('<section class="page stack"><div data-header></div><div data-table></div></section>');
  page.querySelector('[data-header]').replaceWith(PageHeader({
    eyebrow: 'AGENT',
    title: 'Przypisane do mnie',
    description: 'Twoja aktualna lista obslugi.'
  }));
  page.querySelector('[data-table]').replaceWith(TicketTable({ tickets }));
  return page;
}
