import { PageHeader } from '../../components/common/PageHeader.js';
import { TicketForm } from '../../components/tickets/TicketForm.js';
import { htmlToElement } from '../../utils/dom.js';

export function CreateTicketPage({ navigate, showToast }) {
  const page = htmlToElement('<section class="page stack"><div data-header></div><div data-form></div></section>');
  page.querySelector('[data-header]').replaceWith(PageHeader({
    eyebrow: 'Nowe zgloszenie',
    title: 'Utworz ticket',
    description: 'Formularz wysyla pola zgodne z CreateTicketRequest backendu.'
  }));
  page.querySelector('[data-form]').replaceWith(TicketForm({ navigate, showToast }));
  return page;
}
