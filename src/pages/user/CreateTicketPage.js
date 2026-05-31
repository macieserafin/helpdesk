import { getActiveCategories } from '../../api/categoryApi.js';
import { PageHeader } from '../../components/common/PageHeader.js';
import { TicketForm } from '../../components/tickets/TicketForm.js';
import { htmlToElement } from '../../utils/dom.js';

export async function CreateTicketPage({ navigate, showToast }) {
  const categories = await getActiveCategories();
  const page = htmlToElement('<section class="page stack"><div data-header></div><div data-form></div></section>');
  page.querySelector('[data-header]').replaceWith(PageHeader({
    eyebrow: 'Nowe zgloszenie',
    title: 'Utworz ticket',
    description: 'Wybierz aktywna kategorie i opisz problem.'
  }));
  page.querySelector('[data-form]').replaceWith(TicketForm({ categories, navigate, showToast }));
  return page;
}
