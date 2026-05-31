import * as agentApi from '../../api/agentApi.js';
import { getActiveCategories } from '../../api/categoryApi.js';
import { PageHeader } from '../../components/common/PageHeader.js';
import { Pagination } from '../../components/tickets/Pagination.js';
import { TicketFilters } from '../../components/tickets/TicketFilters.js';
import { TicketPriorityForm } from '../../components/tickets/TicketPriorityForm.js';
import { TicketTable } from '../../components/tickets/TicketTable.js';
import { TicketStatusForm } from '../../components/tickets/TicketStatusForm.js';
import { htmlToElement } from '../../utils/dom.js';
import { pageContent, pageMeta } from '../../utils/pageResponse.js';

export async function TicketQueuePage({ showToast }) {
  const categories = await getActiveCategories();
  const page = htmlToElement('<section class="page stack"><div data-header></div><div data-filters></div><div data-table></div><div data-pagination></div></section>');
  let filters = {};
  let pageIndex = 0;

  async function load() {
    const response = await agentApi.getTicketQueue({ ...filters, page: pageIndex, size: 20 });
    const meta = pageMeta(response);
    const tickets = pageContent(response);
    const table = TicketTable({
      tickets,
      actions: (ticket) => `
        <button class="button button-small" data-assign="${ticket.id}">Przypisz</button>
        <span data-priority-slot="${ticket.id}"></span>
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
      const prioritySlot = table.querySelector(`[data-priority-slot="${ticket.id}"]`);
      prioritySlot.replaceWith(TicketPriorityForm({
        currentPriority: ticket.priority,
        onChange: async (priority) => {
          try {
            await agentApi.updateTicketPriority(ticket.id, priority);
            showToast('Priorytet zostal zmieniony.', 'success');
            await load();
          } catch (error) {
            showToast(error.message, 'error');
          }
        }
      }));
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
    const filterNode = TicketFilters({
      filters,
      categories,
      showAgent: true,
      onChange: async (nextFilters) => {
        filters = nextFilters;
        pageIndex = 0;
        await load();
      },
      onReset: async () => {
        filters = {};
        pageIndex = 0;
        await load();
      }
    });
    const pagination = Pagination({
      page: meta,
      onPageChange: async (nextPage) => {
        pageIndex = nextPage;
        await load();
      }
    });
    (page.querySelector('[data-filters]') || page.querySelector('.filter-grid')).replaceWith(filterNode);
    const old = page.querySelector('[data-table]') || page.querySelector('.table-wrap') || page.querySelector('.empty-state');
    old.replaceWith(table);
    (page.querySelector('[data-pagination]') || page.querySelector('.pagination')).replaceWith(pagination);
  }

  page.querySelector('[data-header]').replaceWith(PageHeader({
    eyebrow: 'AGENT',
    title: 'Kolejka ticketow',
    description: 'Widok wszystkich zgloszen dostepnych dla zespolu wsparcia.'
  }));
  await load();
  return page;
}
