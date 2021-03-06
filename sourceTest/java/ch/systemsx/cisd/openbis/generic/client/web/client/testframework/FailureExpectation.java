/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.testframework;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;

/**
 * Failure expectation.
 * 
 * @author    Franz-Josef Elmer
 */
public class FailureExpectation extends AbstractDefaultTestCommand
{

    private IMessageValidator messageValidator = new IMessageValidator()
        {
            public void assertValid(String message)
            {
            }
        };

    private Class<? extends Throwable> expectedThrowableClassOrNull;

    public FailureExpectation(Class<? extends AsyncCallback<?>> callbackClass)
    {
        super(callbackClass);
    }

    public FailureExpectation with(final String failureMessage)
    {
        return with(new IMessageValidator()
            {
                public void assertValid(String message)
                {
                    assertEquals(failureMessage, message);
                }
            });
    }

    public FailureExpectation with(IMessageValidator validator)
    {
        messageValidator = validator;
        return this;
    }

    public FailureExpectation ofType(Class<? extends Throwable> throwableClass)
    {
        expectedThrowableClassOrNull = throwableClass;
        return this;
    }

    @Override
    public List<AbstractAsyncCallback<Object>> tryValidOnFailure(
            List<AbstractAsyncCallback<Object>> callbackObjects, String failureMessage,
            Throwable throwable)
    {
        List<AbstractAsyncCallback<Object>> unmatchedCallbacks =
                tryGetUnmatchedCallbacks(callbackObjects);
        if (unmatchedCallbacks == null)
        {
            return null;
        }
        messageValidator.assertValid(failureMessage);
        if (expectedThrowableClassOrNull == null
                || expectedThrowableClassOrNull.equals(throwable.getClass()))
        {
            return unmatchedCallbacks;
        } else
        {
            return null;
        }
    }

    @Override
    public List<AbstractAsyncCallback<Object>> tryValidOnSucess(
            List<AbstractAsyncCallback<Object>> callbackObjects, Object result)
    {
        return null;
    }

    public void execute()
    {
    }

}
