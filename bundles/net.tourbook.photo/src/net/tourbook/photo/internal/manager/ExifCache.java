/*******************************************************************************
 * Copyright (C) 2005, 2020 Wolfgang Schramm and Contributors
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
package net.tourbook.photo.internal.manager;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import net.tourbook.photo.PhotoImageMetadata;

/**
 * Cache for exif meta data.
 */
public class ExifCache {

   /**
    * Cache for exif meta data, key is file path
    */
   private static final Cache<String, PhotoImageMetadata> _exifCache;

   static {

      _exifCache = Caffeine.newBuilder()
            .maximumSize(20000)
            .build();
   }

   public static void clear() {
      _exifCache.cleanUp();
   }

   public static PhotoImageMetadata get(final String imageFilePathName) {
      return _exifCache.getIfPresent(imageFilePathName);
   }

   public static void put(final String imageFilePathName, final PhotoImageMetadata metadata) {
      _exifCache.put(imageFilePathName, metadata);
   }

   /**
    * Remove all cached metadata which starts with the folder path.
    *
    * @param folderPath
    */
   public static void remove(final String folderPath) {

      // remove cached exif data
      for (final String cachedPath : _exifCache.asMap().keySet()) {
         if (cachedPath.startsWith(folderPath)) {
            _exifCache.invalidate(cachedPath);
         }
      }
   }
}
