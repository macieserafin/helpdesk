import { getTickets } from '../../api/adminApi.js';
import { updateStatus } from '../../api/ticketApi.js';
import { PageHeader } from '../../components/common/PageHeader.js';
import { TicketTable } from '../../components/tickets/TicketTable.js';
import { TicketStatusForm } from '../../components/tickets/TicketStatusForm.js';
import { htmlToElement } from '../../utils/dom.js';

export async function AllTicketsPage({ showToast }) {
  const page = htmlToElement('<section class="page stack"><div data-header></div><div data-table></div></section>');
  page.querySelector('[data-header]').replaceWith(PageHeader({
    eyebrow: 'ADMIN',
    title: 'Wszystkie tickety',
    description: 'Globalny widok zgloszen i zmiana statusu przez endpoint agent/status.'
  }));

  async function load() {
    const tickets = await getTickets();
    const table = TicketTable({
      tickets,
      actions: (ticket) => `<span data-status-slot="${ticket.id}"></span>`
    });
    tickets.forEach((ticket) => {
      table.querySelector(`[data-status-slot="${ticket.id}"]`).replaceWith(TicketStatusForm({
        currentStatus: ticket.status,
        onChange: async (status) => {
          try {
            await updateStatus(ticket.id, status);
            showToast('Status ticketa zostal zaktualizowany.', 'success');
            await load();
          } catch (error) {
            showToast(error.message, 'error');
          }
        }
      }));
    });
    const old = page.querySelector('[data-table]') || page.querySelector('.table-wrap') || page.querySelector('.empty-state');
    old.replaceWith(table);
  }

  await load();
  return page;
}
