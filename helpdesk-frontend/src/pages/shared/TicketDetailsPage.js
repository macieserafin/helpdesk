import { listAttachments } from '../../api/attachmentApi.js';
import * as agentApi from '../../api/agentApi.js';
import * as categoryApi from '../../api/categoryApi.js';
import * as ticketApi from '../../api/ticketApi.js';
import { hasRole } from '../../auth/authService.js';
import { PriorityBadge, StatusBadge } from '../../components/common/Badges.js';
import { PageHeader } from '../../components/common/PageHeader.js';
import { CommentPanel } from '../../components/tickets/CommentPanel.js';
import { HistoryTimeline } from '../../components/tickets/HistoryTimeline.js';
import { TicketEditForm } from '../../components/tickets/TicketEditForm.js';
import { TicketPriorityForm } from '../../components/tickets/TicketPriorityForm.js';
import { TicketStatusForm } from '../../components/tickets/TicketStatusForm.js';
import { ROLES } from '../../utils/constants.js';
import { formatDateTime } from '../../utils/dateFormatter.js';
import { escapeHtml, htmlToElement } from '../../utils/dom.js';
import { getErrorMessage } from '../../utils/errorMessage.js';
import { userLoginIdentifier } from '../../utils/userDisplay.js';

export async function TicketDetailsPage({ params, user, showToast, navigate }) {
  const page = htmlToElement('<section class="page"><div class="stack" data-content></div></section>');
  const ticketId = params.id;
  const staffUser = hasRole(user, ROLES.AGENT) || hasRole(user, ROLES.ADMIN);
  const assignablePriorities = staffUser ? await agentApi.getAssignableTicketPriorities() : [];
  let editing = false;
  let categories = null;
  let reloadingFromEvent = false;
  let reloadTimeout = null;

  async function load() {
    const [ticket, comments, history, attachments] = await Promise.all([
      ticketApi.getTicket(ticketId),
      ticketApi.getComments(ticketId),
      ticketApi.getHistory(ticketId),
      listAttachments(ticketId)
    ]);
    const staff = hasRole(user, ROLES.AGENT) || hasRole(user, ROLES.ADMIN);
    const admin = hasRole(user, ROLES.ADMIN);
    const currentLoginIdentifier = userLoginIdentifier(user);
    const assignedToCurrentAgent = ticket.assignedTo === currentLoginIdentifier;
    const terminal = ['CLOSED', 'REJECTED', 'CANCELLED'].includes(ticket.status);
    const canEdit = !terminal && (ticket.createdBy === currentLoginIdentifier || admin);
    if (editing && canEdit && categories === null) {
      categories = await categoryApi.getActiveCategories();
    }

    const content = htmlToElement(`
      <div class="stack">
        <div data-header></div>
        <section class="ticket-details-layout">
          <div class="ticket-main-column stack">
            <article class="card stack">
              <div class="section-title"><h2>Dane ticketa</h2>${StatusBadge(ticket.status)}</div>
              <dl class="details-list">
                <div><dt>Priorytet</dt><dd>${PriorityBadge(ticket.priority)}</dd></div>
                <div><dt>Kategoria</dt><dd>${escapeHtml(ticket.category)}</dd></div>
                <div><dt>Autor</dt><dd>${escapeHtml(ticket.createdBy)}</dd></div>
                <div><dt>Agent</dt><dd>${escapeHtml(ticket.assignedTo || 'Nieprzypisany')}</dd></div>
                <div><dt>Utworzono</dt><dd>${formatDateTime(ticket.createdAt)}</dd></div>
                <div><dt>Aktualizacja</dt><dd>${formatDateTime(ticket.updatedAt)}</dd></div>
              </dl>
              <p class="ticket-description">${escapeHtml(ticket.description)}</p>
            </article>
            <div data-comments></div>
          </div>
          <aside class="ticket-side-column stack">
            <section class="card stack">
              <h2>Akcje</h2>
              <div class="stack" data-actions></div>
            </section>
            <div data-history></div>
          </aside>
        </section>
      </div>
    `);

    content.querySelector('[data-header]').replaceWith(PageHeader({
      eyebrow: `Ticket #${ticket.id}`,
      title: ticket.title,
      description: 'Szczegoly, komentarze i historia zmian.'
    }));

    const actions = content.querySelector('[data-actions]');
    if (canEdit) {
      const edit = htmlToElement(`<button class="button button-secondary" type="button">${editing ? 'Ukryj edycje' : 'Edytuj ticket'}</button>`);
      edit.addEventListener('click', async () => {
        editing = !editing;
        await load();
      });
      actions.append(edit);

      if (editing) {
        actions.append(TicketEditForm({
          ticket,
          categories,
          onSubmit: async (payload) => {
            try {
              await ticketApi.updateTicket(ticket.id, payload);
              showToast('Ticket został zaktualizowany.', 'success');
              editing = false;
              await load();
            } catch (error) {
              showToast(getErrorMessage(error), 'error');
            }
          },
          onCancel: async () => {
            editing = false;
            await load();
          },
          onError: (error) => showToast(getErrorMessage(error), 'error')
        }));
      }
    }
    if (hasRole(user, ROLES.AGENT) && ticket.status === 'OPEN') {
      const assign = htmlToElement('<button class="button button-secondary" type="button">Przypisz do mnie</button>');
      if (assignedToCurrentAgent) {
        assign.disabled = true;
        assign.textContent = 'Przypisany do Ciebie';
        assign.title = 'Ten ticket jest juz przypisany do Ciebie.';
      }
      assign.addEventListener('click', async () => {
        if (assignedToCurrentAgent) {
          showToast('Ten ticket jest juz przypisany do Ciebie.', 'warning');
          return;
        }
        const originalText = assign.textContent;
        assign.disabled = true;
        assign.textContent = 'Przypisuję...';
        try {
          await agentApi.assignTicket(ticket.id);
          showToast('Ticket został przypisany.', 'success');
          await load();
        } catch (error) {
          showToast(getErrorMessage(error), 'error');
        } finally {
          assign.disabled = false;
          assign.textContent = originalText;
        }
      });
      actions.append(assign);
    }
    actions.append(TicketStatusForm({
      ticket,
      user,
      onChange: async (status) => {
        try {
          if (staff) {
            await agentApi.updateTicketStatus(ticket.id, status);
          } else {
            await ticketApi.updateStatus(ticket.id, status);
          }
          showToast('Status został zmieniony.', 'success');
          editing = false;
          await load();
        } catch (error) {
          showToast(getErrorMessage(error), 'error');
        }
      }
    }));
    if (!staff && ticket.status === 'RESOLVED' && ticket.createdBy === currentLoginIdentifier) {
      const close = htmlToElement('<button class="button button-primary" type="button">Zamknij ticket</button>');
      close.addEventListener('click', async () => {
        const originalText = close.textContent;
        close.disabled = true;
        close.textContent = 'Zamykam...';
        try {
          await ticketApi.updateStatus(ticket.id, 'CLOSED');
          showToast('Ticket został zamknięty.', 'success');
          editing = false;
          await load();
        } catch (error) {
          showToast(getErrorMessage(error), 'error');
        } finally {
          close.disabled = false;
          close.textContent = originalText;
        }
      });
      actions.append(close);
    }
    if (staff) {
      actions.append(TicketPriorityForm({
        currentPriority: ticket.priority,
        priorities: assignablePriorities,
        onChange: async (priority) => {
          try {
            await agentApi.updateTicketPriority(ticket.id, priority);
            showToast('Priorytet został zmieniony.', 'success');
            await load();
          } catch (error) {
            showToast(getErrorMessage(error), 'error');
          }
        }
      }));
    }
    const back = htmlToElement('<button class="button button-ghost" type="button">Wróć</button>');
    back.addEventListener('click', () => window.history.back());
    actions.append(back);

    content.querySelector('[data-comments]').replaceWith(CommentPanel({
      ticketId: ticket.id,
      comments,
      attachments,
      user,
      staff,
      showToast,
      onSaved: load
    }));
    content.querySelector('[data-history]').replaceWith(HistoryTimeline({ history }));
    page.querySelector('[data-content]').replaceChildren(...content.childNodes);
  }

  await load();
  const ticketEvents = ticketApi.openTicketEvents(ticketId);
  const closeTicketEvents = () => {
    if (reloadTimeout) {
      window.clearTimeout(reloadTimeout);
    }
    ticketEvents.close();
  };
  window.addEventListener('hashchange', closeTicketEvents, { once: true });

  async function reloadAfterTicketChange() {
    if (!page.isConnected || reloadingFromEvent) {
      return;
    }

    reloadingFromEvent = true;
    try {
      editing = false;
      await load();
      showToast('Ticket zostal odswiezony po zmianie.', 'info');
    } catch (error) {
      showToast(getErrorMessage(error, 'Nie udalo sie odswiezyc ticketa po zmianie.'), 'error');
    } finally {
      reloadingFromEvent = false;
    }
  }

  ticketEvents.addEventListener('ticket-change', () => {
    if (reloadTimeout) {
      window.clearTimeout(reloadTimeout);
    }
    reloadTimeout = window.setTimeout(reloadAfterTicketChange, 250);
  });

  ticketEvents.addEventListener('error', () => {
    if (!page.isConnected) {
      closeTicketEvents();
    }
  });

  return page;
}
