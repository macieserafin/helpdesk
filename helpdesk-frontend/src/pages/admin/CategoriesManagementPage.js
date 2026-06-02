import * as categoryApi from '../../api/categoryApi.js';
import { CategoryForm } from '../../components/categories/CategoryForm.js';
import { CategoryTable } from '../../components/categories/CategoryTable.js';
import { confirmAction } from '../../components/common/ConfirmDialog.js';
import { ErrorState, LoadingState } from '../../components/common/Feedback.js';
import { PageHeader } from '../../components/common/PageHeader.js';
import { htmlToElement } from '../../utils/dom.js';
import { getErrorMessage } from '../../utils/errorMessage.js';

export async function CategoriesManagementPage({ showToast }) {
  const page = htmlToElement(`
    <section class="page stack">
      <div data-header></div>
      <div class="split-view">
        <section class="card stack"><div class="section-title"><h2>Slownik</h2></div><div data-table></div></section>
        <div data-form></div>
      </div>
    </section>
  `);
  let selectedCategory = null;

  page.querySelector('[data-header]').replaceWith(PageHeader({
    eyebrow: 'ADMIN',
    title: 'Kategorie',
    description: 'Zarzadzanie aktywnymi i nieaktywnymi kategoriami ticketow.'
  }));

  async function loadTable() {
    const old = page.querySelector('[data-table]') || page.querySelector('.table-wrap') || page.querySelector('.empty-state') || page.querySelector('.state-panel') || page.querySelector('.alert-error');
    const loading = LoadingState('Ładowanie kategorii...');
    old.replaceWith(loading);

    try {
      const categories = await categoryApi.getAdminCategories();
      const table = CategoryTable({
        categories,
        onSelect: async (id) => {
          selectedCategory = categories.find((category) => category.id === id) || await categoryApi.getAdminCategory(id);
          await loadForm();
        },
        onDeactivate: async (id) => {
          const category = categories.find((item) => item.id === id);
          const confirmed = await confirmAction({
            title: 'Dezaktywować kategorię?',
            message: `Kategoria ${category?.name || `#${id}`} zniknie z listy aktywnych kategorii dla nowych ticketów.`,
            confirmText: 'Dezaktywuj'
          });
          if (!confirmed) {
            return;
          }

          try {
            await categoryApi.deleteCategory(id);
            showToast('Kategoria została zdezaktywowana.', 'success');
            selectedCategory = null;
            await loadTable();
            await loadForm();
          } catch (error) {
            showToast(getErrorMessage(error), 'error');
          }
        },
        onActivate: async (id) => {
          try {
            await categoryApi.updateCategory(id, { active: true });
            showToast('Kategoria została aktywowana.', 'success');
            await loadTable();
          } catch (error) {
            showToast(getErrorMessage(error), 'error');
          }
        }
      });
      loading.replaceWith(table);
    } catch (error) {
      const message = getErrorMessage(error, 'Nie udało się załadować kategorii.');
      showToast(message, 'error');
      loading.replaceWith(ErrorState(message));
    }
  }

  async function loadForm() {
    const form = CategoryForm({
      category: selectedCategory,
      onSubmit: async (payload) => {
        try {
          if (selectedCategory) {
            await categoryApi.updateCategory(selectedCategory.id, payload);
          } else {
            await categoryApi.createCategory({
              name: payload.name,
              description: payload.description
            });
          }
          showToast('Kategoria została zapisana.', 'success');
          selectedCategory = null;
          await loadTable();
          await loadForm();
        } catch (error) {
          showToast(getErrorMessage(error), 'error');
        }
      },
      onCancel: async () => {
        selectedCategory = null;
        await loadForm();
      },
      onError: (error) => showToast(getErrorMessage(error), 'error')
    });
    (page.querySelector('[data-form]') || page.querySelector('.split-view > .card.form-grid')).replaceWith(form);
  }

  await loadTable();
  await loadForm();
  return page;
}
