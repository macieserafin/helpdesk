import { listAttachments } from '../../api/attachmentApi.js';
import * as agentApi from '../../api/agentApi.js';
import * as ticketApi from '../../api/ticketApi.js';
import { hasRole } from '../../auth/authService.js';
import { PriorityBadge, StatusBadge } from '../../components/common/Badges.js';
import { PageHeader } from '../../components/common/PageHeader.js';
import { AttachmentPanel } from '../../components/tickets/AttachmentPanel.js';
import { CommentPanel } from '../../components/tickets/CommentPanel.js';
import { HistoryTimeline } from '../../components/tickets/HistoryTimeline.js';
import { TicketPriorityForm } from '../../components/tickets/TicketPriorityForm.js';
import { TicketStatusForm } from '../../components/tickets/TicketStatusForm.js';
import { ROLES } from '../../utils/constants.js';
import { formatDateTime } from '../../utils/dateFormatter.js';
import { escapeHtml, htmlToElement } from '../../utils/dom.js';

export async function TicketDetailsPage({ params, user, showToast, navigate }) {
  const page = htmlToElement('<section class="page stack"><div class="stack" data-content></div></section>');
  const ticketId = params.id;
  const [statuses, assignablePriorities] = await Promise.all([
    ticketApi.getTicketStatuses(),
    agentApi.getAssignableTicketPriorities()
  ]);

  async function load() {
    const [ticket, comments, history, attachments] = await Promise.all([
      ticketApi.getTicket(ticketId),
      ticketApi.getComments(ticketId),
      ticketApi.getHistory(ticketId),
      listAttachments(ticketId)
    ]);
    const staff = hasRole(user, ROLES.AGENT) || hasRole(user, ROLES.ADMIN);
    const assignedToCurrentAgent = ticket.assignedTo === user.username;

    const content = htmlToElement(`
      <div class="stack">
        <div data-header></div>
        <section class="details-grid">
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
          <aside class="card stack">
            <h2>Akcje</h2>
            <div class="stack" data-actions></div>
          </aside>
        </section>
        <div class="details-grid">
          <div class="stack">
            <div data-comments></div>
            <div data-attachments></div>
          </div>
          <div data-history></div>
        </div>
      </div>
    `);

    content.querySelector('[data-header]').replaceWith(PageHeader({
      eyebrow: `Ticket #${ticket.id}`,
      title: ticket.title,
      description: 'Szczegoly, komentarze i historia zmian.'
    }));

    const actions = content.querySelector('[data-actions]');
    if (hasRole(user, ROLES.AGENT)) {
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
        try {
          await agentApi.assignTicket(ticket.id);
          showToast('Ticket zostal przypisany.', 'success');
          await load();
        } catch (error) {
          showToast(error.message, 'error');
        }
      });
      actions.append(assign);
    }
    if (staff) {
      actions.append(TicketStatusForm({
        currentStatus: ticket.status,
        statuses,
        onChange: async (status) => {
          try {
            await ticketApi.updateStatus(ticket.id, status);
            showToast('Status zostal zmieniony.', 'success');
            await load();
          } catch (error) {
            showToast(error.message, 'error');
          }
        }
      }));
    }
    if (hasRole(user, ROLES.AGENT)) {
      actions.append(TicketPriorityForm({
        currentPriority: ticket.priority,
        priorities: assignablePriorities,
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
    }
    if (!staff && ticket.status === 'RESOLVED') {
      const close = htmlToElement('<button class="button button-primary" type="button">Zamknij ticket</button>');
      close.addEventListener('click', async () => {
        try {
          await ticketApi.updateStatus(ticket.id, 'CLOSED');
          showToast('Ticket zostal zamkniety.', 'success');
          await load();
        } catch (error) {
          showToast(error.message, 'error');
        }
      });
      actions.append(close);
    }
    const back = htmlToElement('<button class="button button-ghost" type="button">Wroc</button>');
    back.addEventListener('click', () => window.history.back());
    actions.append(back);

    content.querySelector('[data-comments]').replaceWith(CommentPanel({
      ticketId: ticket.id,
      comments,
      staff,
      showToast,
      onSaved: load
    }));
    content.querySelector('[data-attachments]').replaceWith(AttachmentPanel({
      ticketId: ticket.id,
      attachments,
      user,
      showToast,
      onChanged: load
    }));
    content.querySelector('[data-history]').replaceWith(HistoryTimeline({ history }));
    page.querySelector('[data-content]').replaceChildren(...content.childNodes);
  }

  await load();
  return page;
}
