import * as adminApi from '../../api/adminApi.js';
import { confirmAction } from '../../components/common/ConfirmDialog.js';
import { ErrorState, LoadingState } from '../../components/common/Feedback.js';
import { PageHeader } from '../../components/common/PageHeader.js';
import { UserForm } from '../../components/users/UserForm.js';
import { UserTable } from '../../components/users/UserTable.js';
import { htmlToElement } from '../../utils/dom.js';
import { getErrorMessage } from '../../utils/errorMessage.js';

export async function UsersManagementPage({ showToast }) {
  const page = htmlToElement(`
    <section class="page stack">
      <div data-header></div>
      <div class="split-view">
        <section class="card stack"><div class="section-title"><h2>Lista</h2></div><div data-table></div></section>
        <div data-form></div>
      </div>
    </section>
  `);

  page.querySelector('[data-header]').replaceWith(PageHeader({
    eyebrow: 'ADMIN',
    title: 'Uzytkownicy',
    description: 'Tworzenie, edycja i aktywacja kont technicznych.'
  }));

  async function loadTable() {
    const old = page.querySelector('[data-table]') || page.querySelector('.table-wrap') || page.querySelector('.empty-state') || page.querySelector('.state-panel') || page.querySelector('.alert-error');
    const loading = LoadingState('Ładowanie użytkowników...');
    old.replaceWith(loading);

    try {
      const users = await adminApi.getUsers();
      const table = UserTable({
        users,
        onSelect: loadUserForm,
        onToggle: async (id, enabled) => {
          const user = users.find((item) => item.id === id);
          if (!enabled) {
            const confirmed = await confirmAction({
              title: 'Dezaktywować konto?',
              message: `Konto ${user?.username || `#${id}`} nie będzie mogło się zalogować do systemu.`,
              confirmText: 'Dezaktywuj'
            });
            if (!confirmed) {
              return;
            }
          }

          try {
            await adminApi.updateUserEnabled(id, enabled);
            showToast('Status konta został zmieniony.', 'success');
            await loadTable();
          } catch (error) {
            showToast(getErrorMessage(error), 'error');
          }
        }
      });
      loading.replaceWith(table);
    } catch (error) {
      const message = getErrorMessage(error, 'Nie udało się załadować użytkowników.');
      showToast(message, 'error');
      loading.replaceWith(ErrorState(message));
    }
  }

  async function loadUserForm(id = null) {
    const user = id ? await adminApi.getUser(id) : null;
    const form = UserForm({
      user,
      mode: user ? 'edit' : 'create',
      onSubmit: async (payload) => {
        try {
          if (user) {
            await adminApi.updateUser(user.id, payload);
          } else {
            await adminApi.createUser(payload);
          }
          showToast('Dane użytkownika zapisane.', 'success');
          await loadTable();
          await loadUserForm();
        } catch (error) {
          showToast(getErrorMessage(error), 'error');
        }
      }
    });
    const old = page.querySelector('[data-form]') || page.querySelector('.split-view > .card.form-grid');
    old.replaceWith(form);
  }

  await loadTable();
  await loadUserForm();
  return page;
}
