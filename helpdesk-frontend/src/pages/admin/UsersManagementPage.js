import * as adminApi from '../../api/adminApi.js';
import { PageHeader } from '../../components/common/PageHeader.js';
import { UserForm } from '../../components/users/UserForm.js';
import { UserTable } from '../../components/users/UserTable.js';
import { htmlToElement } from '../../utils/dom.js';

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
    const users = await adminApi.getUsers();
    const table = UserTable({
      users,
      onSelect: loadUserForm,
      onToggle: async (id, enabled) => {
        try {
          await adminApi.updateUserEnabled(id, enabled);
          showToast('Status konta zostal zmieniony.', 'success');
          await loadTable();
        } catch (error) {
          showToast(error.message, 'error');
        }
      }
    });
    const old = page.querySelector('[data-table]') || page.querySelector('.table-wrap') || page.querySelector('.empty-state');
    old.replaceWith(table);
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
          showToast('Dane uzytkownika zapisane.', 'success');
          await loadTable();
          await loadUserForm();
        } catch (error) {
          showToast(error.message, 'error');
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
