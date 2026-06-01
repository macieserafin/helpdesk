import { getTickets } from '../../api/adminApi.js';
import { getAdminCategories } from '../../api/categoryApi.js';
import { getTicketPriorities, getTicketStatuses, updateStatus } from '../../api/ticketApi.js';
import { PageHeader } from '../../components/common/PageHeader.js';
import { Pagination } from '../../components/tickets/Pagination.js';
import { TicketFilters } from '../../components/tickets/TicketFilters.js';
import { TicketTable } from '../../components/tickets/TicketTable.js';
import { TicketStatusForm } from '../../components/tickets/TicketStatusForm.js';
import { htmlToElement } from '../../utils/dom.js';
import { pageContent, pageMeta } from '../../utils/pageResponse.js';

export async function AllTicketsPage({ showToast }) {
  const [categories, statuses, priorities] = await Promise.all([
    getAdminCategories(),
    getTicketStatuses(),
    getTicketPriorities()
  ]);
  const page = htmlToElement('<section class="page stack"><div data-header></div><div data-filters></div><div data-table></div><div data-pagination></div></section>');
  let filters = {};
  let pageIndex = 0;

  page.querySelector('[data-header]').replaceWith(PageHeader({
    eyebrow: 'ADMIN',
    title: 'Wszystkie tickety',
    description: 'Globalny widok zgloszen z filtrami i paginacja.'
  }));

  async function load() {
    const response = await getTickets({ ...filters, page: pageIndex, size: 20 });
    const meta = pageMeta(response);
    const tickets = pageContent(response);
    const table = TicketTable({
      tickets,
      actions: (ticket) => `<span data-status-slot="${ticket.id}"></span>`
    });
    tickets.forEach((ticket) => {
      table.querySelector(`[data-status-slot="${ticket.id}"]`).replaceWith(TicketStatusForm({
        currentStatus: ticket.status,
        statuses,
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
    const filterNode = TicketFilters({
      filters,
      categories,
      statuses,
      priorities,
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

  await load();
  return page;
}
