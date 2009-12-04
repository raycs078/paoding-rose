/*
 * Copyright 2007-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.paoding.rose.mock.web.instruction;

import javax.servlet.ServletRequest;

import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.instruction.InstructionExecutor;

/**
 * 
 * @author zhiliang.wang
 * 
 */
public class MockInstructionExecutor implements InstructionExecutor {

    public static final String INSTRUCTION = MockInstructionExecutor.class.getName()
            + ".INSTRUCTION";

    private boolean storesInstructionInRequest;

    public void setStoresInstructionInRequest(boolean storesInstructionToRequest) {
        this.storesInstructionInRequest = storesInstructionToRequest;
    }

    public boolean isStoresInstructionInRequest() {
        return storesInstructionInRequest;
    }

    @Override
    public Object render(Invocation inv, Object instruction) throws Exception {
        System.out.println(getClass().getSimpleName() + "-------instruction=" + instruction);
        // 设置到request中，使测试用例可以从request对象取回instruction对象
        if (storesInstructionInRequest) {
            inv.getRequest().setAttribute(INSTRUCTION, instruction);
        }
        return instruction;
    }

    public Object getInstruction(ServletRequest request) {
        if (!storesInstructionInRequest) {
            throw new UnsupportedOperationException(
                    "please set storesInstructionToRequest=true firset");
        }
        return request.getAttribute(INSTRUCTION);
    }

}
