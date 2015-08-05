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
package org.thingml.compilers.cpp.sintefboard;

import org.sintef.thingml.Message;
import org.sintef.thingml.Port;
import org.sintef.thingml.Thing;
import org.thingml.compilers.c.CCompilerContext;
import org.thingml.compilers.c.CThingApiCompiler;

/**
 * Created by ffl on 17.06.15.
 */
public class CThingApiCompilerSintefboard extends CThingApiCompiler {

    @Override
    public boolean isGeneratingCpp() {
        return true;
    }

    @Override
    public String getCppNameScope() {
        return "/*CFG_CPPNAME_SCOPE*/";
    }

    @Override
    protected void generatePublicMessageSendingOperations(Thing thing, StringBuilder builder, CCompilerContext ctx) {
        builder.append("// Declaration of callbacks for incoming messages:\n");
        for(Port port : thing.allPorts()) {
            for (Message msg : port.getSends()) {
                builder.append("void register_" + ctx.getSenderName(thing, port, msg) + "_listener(");
                builder.append("void (" + getCppNameScope() + "*_listener)");
                ctx.appendFormalTypeSignature(thing, builder, msg);
                builder.append(");\n");

            }
        }
        builder.append("\n");
    }



}