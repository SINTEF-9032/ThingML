/**
 * Copyright (C) 2014 SINTEF <franck.fleurey@sintef.no>
 *
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thingml.compilers.debugGUI;

import org.thingml.compilers.Context;
import org.thingml.compilers.NetworkLibraryGenerator;
import org.thingml.compilers.ThingMLCompiler;

import java.util.Set;

/**
 *
 * @author sintef
 */
public class DebugGUICompilerContext extends Context {

    private Set<NetworkLibraryGenerator> NetworkLibraryGenerators;

    public DebugGUICompilerContext(ThingMLCompiler compiler) {
        super(compiler);
    }

    public String getHtmlTemplate() {
        return getTemplateByID("debugGUItemplates/mockup-html.html");
    }

    public String getCSSTemplate() {
        return getTemplateByID("debugGUItemplates/mockup-css.css");
    }

    public Set<NetworkLibraryGenerator> getNetworkLibraryGenerators() {
        return NetworkLibraryGenerators;
    }

    public void addNetworkLibraryGenerator(NetworkLibraryGenerator nlg) {
        NetworkLibraryGenerators.add(nlg);
    }

}
