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
package org.operaton.bpm.engine.cdi.impl.el;

import jakarta.el.ELContext;
import org.operaton.bpm.engine.cdi.impl.util.BeanManagerLookup;
import org.operaton.bpm.engine.cdi.impl.util.ProgrammaticBeanLookup;
import jakarta.el.ELResolver;

import jakarta.enterprise.inject.spi.BeanManager;


/**
 * Resolver wrapping an instance of jakarta.el.ELResolver obtained from the
 * {@link BeanManager}. Allows the process engine to resolve Cdi-Beans.
 *
 * @author Daniel Meyer
 */
public class CdiResolver extends ELResolver {

  protected BeanManager getBeanManager() {
    return BeanManagerLookup.getBeanManager();
  }

  protected ELResolver getWrappedResolver() {
    BeanManager beanManager = getBeanManager();
    return beanManager.getELResolver();
  }

  @Override
  public Class< ? > getCommonPropertyType(ELContext context, Object base) {
    return getWrappedResolver().getCommonPropertyType(context, base);
  }

  @Override
  public Class< ? > getType(ELContext context, Object base, Object property) {
    return getWrappedResolver().getType(context, base, property);
  }

  @Override
  public Object getValue(ELContext context, Object base, Object property) {
    //we need to resolve a bean only for the first "member" of expression, e.g. bean.property1.property2
    if (base == null) {
      Object result = ProgrammaticBeanLookup.lookup(property.toString(), getBeanManager());
      if (result != null) {
        context.setPropertyResolved(true);
      }
      return result;
    } else {
      return null;
    }
  }

  @Override
  public boolean isReadOnly(ELContext context, Object base, Object property) {
    return getWrappedResolver().isReadOnly(context, base, property);
  }

  @Override
  public void setValue(ELContext context, Object base, Object property, Object value) {
    getWrappedResolver().setValue(context, base, property, value);
  }

  @Override
  public Object invoke(ELContext context, Object base, Object method, java.lang.Class< ? >[] paramTypes, Object[] params) {
    return getWrappedResolver().invoke(context, base, method, paramTypes, params);
  }

}
