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
package net.paoding.rose.web.portal;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public interface WindowTask {

    /**
     * Attempts to cancel execution of this task. This attempt will fail if
     * the task has already completed, has already been cancelled, or could
     * not be cancelled for some other reason. If successful, and this task
     * has not started when <tt>cancel</tt> is called, this task should
     * never run. If the task has already started, then the
     * <tt>mayInterruptIfRunning</tt> parameter determines whether the
     * thread executing this task should be interrupted in an attempt to
     * stop the task.
     * 
     * <p>
     * After this method returns, subsequent calls to {@link #isDone} will
     * always return <tt>true</tt>. Subsequent calls to
     * {@link #isCancelled} will always return <tt>true</tt> if this method
     * returned <tt>true</tt>.
     * 
     * @param mayInterruptIfRunning <tt>true</tt> if the thread executing
     *        this task should be interrupted; otherwise, in-progress tasks
     *        are allowed to complete
     * @return <tt>false</tt> if the task could not be cancelled, typically
     *         because it has already completed normally; <tt>true</tt>
     *         otherwise
     */
    boolean cancel(boolean mayInterruptIfRunning);

    /**
     * Returns <tt>true</tt> if this task was cancelled before it completed
     * normally.
     * 
     * @return <tt>true</tt> if this task was cancelled before it completed
     */
    boolean isCancelled();

    /**
     * Returns <tt>true</tt> if this task completed.
     * 
     * Completion may be due to normal termination, an exception, or
     * cancellation -- in all of these cases, this method will return
     * <tt>true</tt>.
     * 
     * @return <tt>true</tt> if this task completed
     */
    boolean isDone();

    public void await() throws InterruptedException, ExecutionException;

    public void await(long await) throws InterruptedException, ExecutionException, TimeoutException;
}
