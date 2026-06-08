import { getActiveCategories } from '../../api/categoryApi.js';
import { getMyTickets, getTicketPriorities, getTicketStatuses } from '../../api/ticketApi.js';
import { ErrorState, LoadingState } from '../../components/common/Feedback.js';
import { PageHeader } from '../../components/common/PageHeader.js';
import { Pagination } from '../../components/tickets/Pagination.js';
import { TicketFilters } from '../../components/tickets/TicketFilters.js';
import { TicketTable } from '../../components/tickets/TicketTable.js';
import { htmlToElement } from '../../utils/dom.js';
import { getErrorMessage } from '../../utils/errorMessage.js';
import { pageContent, pageMeta } from '../../utils/pageResponse.js';

export async function MyTicketsPage({ showToast }) {
  const [categories, statuses, priorities] = await Promise.all([
    getActiveCategories(),
    getTicketStatuses(),
    getTicketPriorities()
  ]);
  const page = htmlToElement('<section class="page stack"><div data-header></div><div data-filters></div><div data-table></div><div data-pagination></div></section>');
  let filters = {};
  let pageIndex = 0;

  page.querySelector('[data-header]').replaceWith(PageHeader({
    eyebrow: 'USER',
    title: 'Moje tickety',
    description: 'Lista zgłoszeń utworzonych przez aktualnie zalogowanego użytkownika.'
  }));

  async function load() {
    const oldTable = page.querySelector('[data-table]') || page.querySelector('.table-wrap') || page.querySelector('.empty-state') || page.querySelector('.state-panel') || page.querySelector('.alert-error');
    const loading = LoadingState('Ładowanie ticketów...');
    oldTable.replaceWith(loading);

    try {
      const response = await getMyTickets({ ...filters, page: pageIndex, size: 20 });
      const meta = pageMeta(response);
      const tickets = pageContent(response);
      const filterNode = TicketFilters({
        filters,
        categories,
        statuses,
        priorities,
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
      loading.replaceWith(TicketTable({ tickets }));
      (page.querySelector('[data-pagination]') || page.querySelector('.pagination')).replaceWith(pagination);
    } catch (error) {
      const message = getErrorMessage(error, 'Nie udało się załadować ticketów.');
      showToast(message, 'error');
      loading.replaceWith(ErrorState(message));
    }
  }

  await load();
  return page;
}
