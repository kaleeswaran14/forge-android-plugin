/*
* Copyright 2013 JUGChennai
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package in.jugchennai.forge.android;

import org.jboss.forge.project.facets.BaseFacet;
import org.jboss.forge.project.facets.DependencyFacet;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.RequiresFacet;

/**
 * 
 * <b>Contributors</b>
 * 
 * @author Rajmahendra Hegde <rajmahendra@gmail.com>
 *
 */

@Alias("org.android")
@RequiresFacet({DependencyFacet.class})
public class AndroidFacet extends BaseFacet {

	@Override
	public boolean install() {
		
		return false;
	}

	@Override
	public boolean isInstalled() {
		
		return false;
	}
	
	@Override
	public boolean uninstall() {
		
		return super.uninstall();
	}

}
