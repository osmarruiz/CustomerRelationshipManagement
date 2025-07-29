import React from 'react';
import MenuItem from 'app/shared/layout/menus/menu-item';

import { translate } from 'react-jhipster';
import { NavDropdown } from './menu-components';

const adminMenuItems = () => (
  <>
    <MenuItem icon="road" to="/admin/gateway">
      Gateway
    </MenuItem>
    <MenuItem icon="tachometer-alt" to="/admin/metrics">
      Metrics
    </MenuItem>
    <MenuItem icon="heart" to="/admin/health">
      Health
    </MenuItem>
    <MenuItem icon="cogs" to="/admin/configuration">
      Configuration
    </MenuItem>
    <MenuItem icon="tasks" to="/admin/logs">
      Logs
    </MenuItem>
    {/* jhipster-needle-add-element-to-admin-menu - JHipster will add entities to the admin menu here */}
  </>
);

const openAPIItem = () => (
  <MenuItem icon="book" to="/admin/docs">
    API
  </MenuItem>
);

export const AdminMenu = ({ showOpenAPI }) => (
  <NavDropdown icon="users-cog" name={translate('global.menu.admin.main')} id="admin-menu" data-cy="adminMenu">
    {adminMenuItems()}
    {showOpenAPI && openAPIItem()}
  </NavDropdown>
);

export default AdminMenu;
