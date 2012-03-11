/**
 * Copyright (C) 2012 Julien Nicoulaud <julien.nicoulaud@gmail.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package net.nicoulaj.visualvm.plugins.hidejstatd;

import com.sun.tools.visualvm.application.Application;
import com.sun.tools.visualvm.application.type.ApplicationTypeFactory;
import com.sun.tools.visualvm.core.datasource.DataSourceRepository;
import com.sun.tools.visualvm.core.datasupport.ComparableWeakReference;
import com.sun.tools.visualvm.core.datasupport.DataChangeEvent;
import com.sun.tools.visualvm.core.datasupport.DataChangeListener;
import java.util.HashSet;
import java.util.Set;
import org.openide.modules.ModuleInstall;

/**
 * Quick'n'dirty module that matches {@link Application}s by name and hides Jstatd instances.
 *
 * @author Julien Nicoulaud <julien.nicoulaud@gmail.com>
 * @since 0.1
 */
public final class HideJStatdApplicationsModule extends ModuleInstall {

    /**
     * The name given the {@link Application}s recognized as Jstatd instances.
     *
     * @see ApplicationTypeFactory
     */
    private static final String JSTATD_APP_NAME = "Jstatd";
    /**
     * The listerner for {@link DataSourceRepository} changes.
     */
    private ApplicationRepositoryChangeListener listener = new ApplicationRepositoryChangeListener();
    /**
     * The {@link Set} of {@link Application} we messed up.
     */
    private Set<ComparableWeakReference<Application>> hiddenApps;

    /**
     * Register our {@link #listener} and init {@link #hiddenApps}.
     */
    @Override
    public void restored() {
        DataSourceRepository.sharedInstance().addDataChangeListener(listener, Application.class);
        hiddenApps = new HashSet<ComparableWeakReference<Application>>();
    }

    /**
     * Unregister our {@link #listener} and restore/clear {@link #hiddenApps}.
     */
    @Override
    public void uninstalled() {
        DataSourceRepository.sharedInstance().removeDataChangeListener(listener);
        for (ComparableWeakReference<Application> ref : hiddenApps) {
            final Application app = ref.get();
            if (app != null)
                app.setVisible(true);
        }
        hiddenApps = null;
    }

    /**
     * Listener that hides news apps which names matches {@link HideJStatdApplicationsModule#JSTATD_APP_NAME} and stores them in
     * {@link HideJStatdApplicationsModule#hiddenApps}.
     */
    private final class ApplicationRepositoryChangeListener implements DataChangeListener<Application> {

        @Override
        public void dataChanged(DataChangeEvent<Application> dce) {
            if (hiddenApps != null)
                for (Application app : dce.getAdded())
                    if (app.isVisible() && JSTATD_APP_NAME.equals(ApplicationTypeFactory.getApplicationTypeFor(app).getName())) {
                        hiddenApps.add(new ComparableWeakReference<Application>(app));
                        app.setVisible(false);
                    }
        }
    }
}
