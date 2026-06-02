import * as categoryApi from '../../api/categoryApi.js';
import { CategoryForm } from '../../components/categories/CategoryForm.js';
import { CategoryTable } from '../../components/categories/CategoryTable.js';
import { PageHeader } from '../../components/common/PageHeader.js';
import { htmlToElement } from '../../utils/dom.js';

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
    const categories = await categoryApi.getAdminCategories();
    const table = CategoryTable({
      categories,
      onSelect: async (id) => {
        selectedCategory = categories.find((category) => category.id === id) || await categoryApi.getAdminCategory(id);
        await loadForm();
      },
      onDeactivate: async (id) => {
        try {
          await categoryApi.deleteCategory(id);
          showToast('Kategoria zostala zdezaktywowana.', 'success');
          selectedCategory = null;
          await loadTable();
          await loadForm();
        } catch (error) {
          showToast(error.message, 'error');
        }
      },
      onActivate: async (id) => {
        try {
          await categoryApi.updateCategory(id, { active: true });
          showToast('Kategoria zostala aktywowana.', 'success');
          await loadTable();
        } catch (error) {
          showToast(error.message, 'error');
        }
      }
    });
    (page.querySelector('[data-table]') || page.querySelector('.table-wrap') || page.querySelector('.empty-state')).replaceWith(table);
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
          showToast('Kategoria zostala zapisana.', 'success');
          selectedCategory = null;
          await loadTable();
          await loadForm();
        } catch (error) {
          showToast(error.message, 'error');
        }
      },
      onCancel: async () => {
        selectedCategory = null;
        await loadForm();
      }
    });
    (page.querySelector('[data-form]') || page.querySelector('.split-view > .card.form-grid')).replaceWith(form);
  }

  await loadTable();
  await loadForm();
  return page;
}
