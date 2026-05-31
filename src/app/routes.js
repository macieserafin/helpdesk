import { ROLES } from '../utils/constants.js';
import { LoginPage } from '../pages/auth/LoginPage.js';
import { UserDashboardPage } from '../pages/user/UserDashboardPage.js';
import { MyTicketsPage } from '../pages/user/MyTicketsPage.js';
import { CreateTicketPage } from '../pages/user/CreateTicketPage.js';
import { ProfilePage } from '../pages/user/ProfilePage.js';
import { AgentDashboardPage } from '../pages/agent/AgentDashboardPage.js';
import { TicketQueuePage } from '../pages/agent/TicketQueuePage.js';
import { AssignedTicketsPage } from '../pages/agent/AssignedTicketsPage.js';
import { AdminDashboardPage } from '../pages/admin/AdminDashboardPage.js';
import { CategoriesManagementPage } from '../pages/admin/CategoriesManagementPage.js';
import { UsersManagementPage } from '../pages/admin/UsersManagementPage.js';
import { AllTicketsPage } from '../pages/admin/AllTicketsPage.js';
import { TicketDetailsPage } from '../pages/shared/TicketDetailsPage.js';
import { NotFoundPage } from '../pages/shared/NotFoundPage.js';

export const routes = [
  { path: '/login', public: true, page: LoginPage },
  { path: '/user', roles: [ROLES.USER], page: UserDashboardPage },
  { path: '/user/tickets', roles: [ROLES.USER], page: MyTicketsPage },
  { path: '/user/tickets/new', roles: [ROLES.USER], page: CreateTicketPage },
  { path: '/user/profile', roles: [ROLES.USER, ROLES.AGENT, ROLES.ADMIN], page: ProfilePage },
  { path: '/agent', roles: [ROLES.AGENT], page: AgentDashboardPage },
  { path: '/agent/tickets', roles: [ROLES.AGENT], page: TicketQueuePage },
  { path: '/agent/assigned', roles: [ROLES.AGENT], page: AssignedTicketsPage },
  { path: '/admin', roles: [ROLES.ADMIN], page: AdminDashboardPage },
  { path: '/admin/users', roles: [ROLES.ADMIN], page: UsersManagementPage },
  { path: '/admin/categories', roles: [ROLES.ADMIN], page: CategoriesManagementPage },
  { path: '/admin/tickets', roles: [ROLES.ADMIN], page: AllTicketsPage },
  { path: '/tickets/:id', roles: [ROLES.USER, ROLES.AGENT, ROLES.ADMIN], page: TicketDetailsPage },
  { path: '*', public: true, page: NotFoundPage }
];
