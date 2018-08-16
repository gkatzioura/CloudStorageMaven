/*
 * Copyright 2018 Emmanouil Gkatziouras
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.ruschecker.listener;

import java.util.Vector;

import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.events.SessionEvent;
import org.apache.maven.wagon.events.SessionListener;

public class SessionListenerContainerImpl implements SessionListenerContainer {

    private final Wagon wagon;
    private final Vector<SessionListener> sessionListeners;

    public SessionListenerContainerImpl(Wagon wagon) {
        this.wagon = wagon;
        sessionListeners = new Vector<>();
    }

    @Override
    public void addSessionListener(SessionListener sessionListener) {
        if(sessionListener==null) {
            throw new NullPointerException();
        }
        if(!sessionListeners.contains(sessionListener)) {
            sessionListeners.add(sessionListener);
        }
    }

    @Override
    public void removeSessionListener(SessionListener sessionListener) {
        sessionListeners.remove(sessionListener);
    }

    @Override
    public boolean hasSessionListener(SessionListener sessionListener) {
        return sessionListeners.contains(sessionListener);
    }

    @Override
    public void fireSessionOpening() {
        SessionEvent sessionEvent = new SessionEvent(this.wagon, SessionEvent.SESSION_OPENING);
        sessionListeners.forEach(e->e.sessionOpening(sessionEvent));
    }

    @Override
    public void fireSessionOpened() {
        SessionEvent sessionEvent = new SessionEvent(this.wagon, SessionEvent.SESSION_OPENED);
        sessionListeners.forEach(e->e.sessionOpened(sessionEvent));
    }

    @Override
    public void fireSessionDisconnecting() {
        SessionEvent sessionEvent = new SessionEvent(this.wagon, SessionEvent.SESSION_DISCONNECTING);
        sessionListeners.forEach(e->e.sessionDisconnecting(sessionEvent));
    }

    @Override
    public void fireSessionDisconnected() {
        SessionEvent sessionEvent = new SessionEvent(this.wagon, SessionEvent.SESSION_DISCONNECTED);
        sessionListeners.forEach(se->se.sessionDisconnected(sessionEvent));
    }

    @Override
    public void fireSessionConnectionRefused() {
        SessionEvent sessionEvent = new SessionEvent(this.wagon, SessionEvent.SESSION_CONNECTION_REFUSED);
        sessionListeners.forEach(se->se.sessionConnectionRefused(sessionEvent));
    }

    @Override
    public void fireSessionLoggedIn() {
        SessionEvent sessionEvent = new SessionEvent(this.wagon, SessionEvent.SESSION_LOGGED_IN);
        sessionListeners.forEach(se->se.sessionLoggedIn(sessionEvent));
    }

    @Override
    public void fireSessionLoggedOff() {
        SessionEvent sessionEvent = new SessionEvent(this.wagon, SessionEvent.SESSION_LOGGED_OFF);
        sessionListeners.forEach(se->se.sessionLoggedOff(sessionEvent));
    }

    @Override
    public void fireSessionError(Exception exception) {
        SessionEvent sessionEvent = new SessionEvent(this.wagon, exception);
        sessionListeners.forEach(se->se.sessionError(sessionEvent));
    }
}
