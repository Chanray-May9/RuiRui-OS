package com.ruirui.os;
public final class RuiVoiceSessionService extends android.service.voice.VoiceInteractionSessionService {
    @Override public android.service.voice.VoiceInteractionSession onNewSession(android.os.Bundle args){return new RuiVoiceSession(this);}
}
