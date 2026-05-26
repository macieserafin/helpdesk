import * as agentApi from '../../api/agentApi.js';
import { PageHeader } from '../../components/common/PageHeader.js';
import { TicketTable } from '../../components/tickets/TicketTable.js';
import { TicketStatusForm } from '../../components/tickets/TicketStatusForm.js';
import { htmlToElement } from '../../utils/dom.js';

export async function TicketQueuePage({ showToast }) {
  const page = htmlToElement('<section class="page stack"><div data-header></div><div data-table></div></section>');

  async function load() {
    const tickets = await agentApi.getTicketQueue();
    const table = TicketTable({
      tickets,
      actions: (ticket) => `
        <button class="button button-small" data-assign="${ticket.id}">Przypisz</button>
        <span data-status-slot="${ticket.id}"></span>
      `
    });
    table.querySelectorAll('[data-assign]').forEach((button) => {
      button.addEventListener('click', async () => {
        try {
          await agentApi.assignTicket(button.dataset.assign);
          showToast('Ticket przypisany do Ciebie.', 'success');
          await load();
        } catch (error) {
          showToast(error.message, 'error');
        }
      });
    });
    tickets.forEach((ticket) => {
      const slot = table.querySelector(`[data-status-slot="${ticket.id}"]`);
      slot.replaceWith(TicketStatusForm({
        currentStatus: ticket.status,
        onChange: async (status) => {
          try {
            await agentApi.updateTicketStatus(ticket.id, status);
            showToast('Status zostal zmieniony.', 'success');
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

  page.querySelector('[data-header]').replaceWith(PageHeader({
    eyebrow: 'AGENT',
    title: 'Kolejka ticketow',
    description: 'Widok wszystkich zgloszen dostepnych dla zespolu wsparcia.'
  }));
  await load();
  return page;
}
