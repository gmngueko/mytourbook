/*******************************************************************************
 * Copyright (C) 2005, 2025 Wolfgang Schramm and Contributors
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110, USA
 *******************************************************************************/
package net.tourbook.export;

import java.util.Optional;

import net.tourbook.common.color.ThemeUtil;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ResourceLocator;
import org.eclipse.osgi.internal.framework.EquinoxBundle;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;

/**
 * The activator class controls the plug-in life cycle
 */
@SuppressWarnings("restriction")
public class Activator extends AbstractUIPlugin {

   // The plug-in ID
   public static final String PLUGIN_ID = "net.tourbook.export"; //$NON-NLS-1$

   // The shared instance
   private static Activator plugin;

   private Version          version;

   /**
    * The constructor
    */
   public Activator() {}

   /**
    * Returns the shared instance
    *
    * @return the shared instance
    */
   public static Activator getDefault() {
      return plugin;
   }

   /**
    * Returns an image descriptor for images in the plug-in path.
    *
    * @param path
    *           the image path
    * @return the image descriptor
    */
   public static ImageDescriptor getImageDescriptor(final String path) {

      final Optional<ImageDescriptor> imageDescriptor = ResourceLocator.imageDescriptorFromBundle(PLUGIN_ID, "icons/" + path); //$NON-NLS-1$

      return imageDescriptor.isPresent() ? imageDescriptor.get() : null;
   }

   /**
    * @param imageName
    *
    * @return Returns the themed image descriptor from {@link Activator} plugin images
    */
   public static ImageDescriptor getThemedImageDescriptor(final String imageName) {

      return getImageDescriptor(ThemeUtil.getThemedImageName(imageName));
   }

   public Version getVersion() {
      return version;
   }

   @Override
   public void start(final BundleContext context) throws Exception {

      super.start(context);
      plugin = this;

      final Bundle bundle = context.getBundle();
      if (bundle instanceof EquinoxBundle) {
         final EquinoxBundle abstractBundle = (EquinoxBundle) bundle;
         version = abstractBundle.getVersion();
      }
   }

   /*
    * (non-Javadoc)
    * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
    */
   @Override
   public void stop(final BundleContext context) throws Exception {
      plugin = null;
      super.stop(context);
   }
}
