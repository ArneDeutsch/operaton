/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.operaton.bpm.engine.impl.identity.db;

import static org.operaton.bpm.engine.impl.util.EncryptionUtil.saltPassword;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.operaton.bpm.engine.authorization.Permission;
import org.operaton.bpm.engine.authorization.Permissions;
import org.operaton.bpm.engine.authorization.Resource;
import org.operaton.bpm.engine.authorization.Resources;
import org.operaton.bpm.engine.identity.Group;
import org.operaton.bpm.engine.identity.GroupQuery;
import org.operaton.bpm.engine.identity.NativeUserQuery;
import org.operaton.bpm.engine.identity.Tenant;
import org.operaton.bpm.engine.identity.TenantQuery;
import org.operaton.bpm.engine.identity.User;
import org.operaton.bpm.engine.identity.UserQuery;
import org.operaton.bpm.engine.impl.AbstractQuery;
import org.operaton.bpm.engine.impl.NativeUserQueryImpl;
import org.operaton.bpm.engine.impl.UserQueryImpl;
import org.operaton.bpm.engine.impl.context.Context;
import org.operaton.bpm.engine.impl.identity.ReadOnlyIdentityProvider;
import org.operaton.bpm.engine.impl.interceptor.CommandContext;
import org.operaton.bpm.engine.impl.persistence.AbstractManager;
import org.operaton.bpm.engine.impl.persistence.entity.GroupEntity;
import org.operaton.bpm.engine.impl.persistence.entity.TenantEntity;
import org.operaton.bpm.engine.impl.persistence.entity.UserEntity;

/**
 * <p>Read only implementation of DB-backed identity service</p>
 *
 * @author Daniel Meyer
 * @author nico.rehwaldt
 */
@SuppressWarnings("unchecked")
public class DbReadOnlyIdentityServiceProvider extends AbstractManager implements ReadOnlyIdentityProvider {

  // users /////////////////////////////////////////

  @Override
  public UserEntity findUserById(String userId) {
    checkAuthorization(Permissions.READ, Resources.USER, userId);
    return getDbEntityManager().selectById(UserEntity.class, userId);
  }

  @Override
  public UserQuery createUserQuery() {
    return new DbUserQueryImpl(Context.getProcessEngineConfiguration().getCommandExecutorTxRequired());
  }

  @Override
  public UserQueryImpl createUserQuery(CommandContext commandContext) {
    return new DbUserQueryImpl();
  }

  @Override public NativeUserQuery createNativeUserQuery() {
    return new NativeUserQueryImpl(Context.getProcessEngineConfiguration().getCommandExecutorTxRequired());
  }

  public long findUserCountByQueryCriteria(DbUserQueryImpl query) {
    configureQuery(query, Resources.USER);
    return (Long) getDbEntityManager().selectOne("selectUserCountByQueryCriteria", query);
  }

  public List<User> findUserByQueryCriteria(DbUserQueryImpl query) {
    configureQuery(query, Resources.USER);
    return getDbEntityManager().selectList("selectUserByQueryCriteria", query);
  }

  public List<User> findUserByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return getDbEntityManager().selectListWithRawParameter("selectUserByNativeQuery", parameterMap, firstResult, maxResults);
  }

  public long findUserCountByNativeQuery(Map<String, Object> parameterMap) {
    return (Long) getDbEntityManager().selectOne("selectUserCountByNativeQuery", parameterMap);
  }

  @Override
  public boolean checkPassword(String userId, String password) {
    UserEntity user = findUserById(userId);
    if ((user != null) && (password != null) && matchPassword(password, user)) {
      return true;
    } else {
      return false;
    }
  }

  protected boolean matchPassword(String password, UserEntity user) {
    String saltedPassword = saltPassword(password, user.getSalt());
    return Context.getProcessEngineConfiguration()
      .getPasswordManager()
      .check(saltedPassword, user.getPassword());
  }

  // groups //////////////////////////////////////////

  @Override
  public GroupEntity findGroupById(String groupId) {
    checkAuthorization(Permissions.READ, Resources.GROUP, groupId);
    return getDbEntityManager().selectById(GroupEntity.class, groupId);
  }

  @Override
  public GroupQuery createGroupQuery() {
    return new DbGroupQueryImpl(Context.getProcessEngineConfiguration().getCommandExecutorTxRequired());
  }

  @Override
  public GroupQuery createGroupQuery(CommandContext commandContext) {
    return new DbGroupQueryImpl();
  }

  public long findGroupCountByQueryCriteria(DbGroupQueryImpl query) {
    configureQuery(query, Resources.GROUP);
    return (Long) getDbEntityManager().selectOne("selectGroupCountByQueryCriteria", query);
  }

  public List<Group> findGroupByQueryCriteria(DbGroupQueryImpl query) {
    configureQuery(query, Resources.GROUP);
    return getDbEntityManager().selectList("selectGroupByQueryCriteria", query);
  }

  //tenants //////////////////////////////////////////

  @Override
  public TenantEntity findTenantById(String tenantId) {
    checkAuthorization(Permissions.READ, Resources.TENANT, tenantId);
    return getDbEntityManager().selectById(TenantEntity.class, tenantId);
  }

  @Override
  public TenantQuery createTenantQuery() {
    return new DbTenantQueryImpl(Context.getProcessEngineConfiguration().getCommandExecutorTxRequired());
  }

  @Override
  public TenantQuery createTenantQuery(CommandContext commandContext) {
    return new DbTenantQueryImpl();
  }

  public long findTenantCountByQueryCriteria(DbTenantQueryImpl query) {
    configureQuery(query, Resources.TENANT);
    return (Long) getDbEntityManager().selectOne("selectTenantCountByQueryCriteria", query);
  }

  public List<Tenant> findTenantByQueryCriteria(DbTenantQueryImpl query) {
    configureQuery(query, Resources.TENANT);
    return getDbEntityManager().selectList("selectTenantByQueryCriteria", query);
  }

  //memberships //////////////////////////////////////////
  protected boolean existsMembership(String userId, String groupId) {
    Map<String, String> key = new HashMap<>();
    key.put("userId", userId);
    key.put("groupId", groupId);
    return ((Long) getDbEntityManager().selectOne("selectMembershipCount", key)) > 0;
  }

  protected boolean existsTenantMembership(String tenantId, String userId, String groupId) {
    Map<String, String> key = new HashMap<>();
    key.put("tenantId", tenantId);
    if (userId != null) {
      key.put("userId", userId);
    }
    if (groupId != null) {
      key.put("groupId", groupId);
    }
    return ((Long) getDbEntityManager().selectOne("selectTenantMembershipCount", key)) > 0;
  }

  //authorizations ////////////////////////////////////////////////////

  @Override
  protected void configureQuery(@SuppressWarnings("rawtypes") AbstractQuery query, Resource resource) {
    Context.getCommandContext()
      .getAuthorizationManager()
      .configureQuery(query, resource);
  }

  @Override
  protected void checkAuthorization(Permission permission, Resource resource, String resourceId) {
    Context.getCommandContext()
      .getAuthorizationManager()
      .checkAuthorization(permission, resource, resourceId);
 }

}
