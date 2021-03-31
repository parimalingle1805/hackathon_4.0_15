package net.sourceforge.opencamera;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.util.Log;
import java.util.ArrayList;
import java.util.Locale;
import net.sourceforge.opencamera.cameracontroller.CameraController;

class SpeechControl {
    private static final String TAG = "SpeechControl";
    /* access modifiers changed from: private */
    public final MainActivity main_activity;
    private SpeechRecognizer speechRecognizer;
    /* access modifiers changed from: private */
    public boolean speechRecognizerIsStarted;

    SpeechControl(MainActivity mainActivity) {
        this.main_activity = mainActivity;
    }

    /* access modifiers changed from: 0000 */
    public void startSpeechRecognizerIntent() {
        if (this.speechRecognizer != null) {
            Intent intent = new Intent("android.speech.action.RECOGNIZE_SPEECH");
            intent.putExtra("android.speech.extra.LANGUAGE", "en_US");
            this.speechRecognizer.startListening(intent);
        }
    }

    /* access modifiers changed from: 0000 */
    public void speechRecognizerStarted() {
        this.main_activity.getMainUI().audioControlStarted();
        this.speechRecognizerIsStarted = true;
    }

    private void speechRecognizerStopped() {
        this.main_activity.getMainUI().audioControlStopped();
        this.speechRecognizerIsStarted = false;
    }

    /* access modifiers changed from: 0000 */
    public void initSpeechRecognizer() {
        boolean equals = PreferenceManager.getDefaultSharedPreferences(this.main_activity).getString(PreferenceKeys.AudioControlPreferenceKey, CameraController.COLOR_EFFECT_DEFAULT).equals("voice");
        if (this.speechRecognizer == null && equals) {
            this.speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this.main_activity);
            SpeechRecognizer speechRecognizer2 = this.speechRecognizer;
            if (speechRecognizer2 != null) {
                this.speechRecognizerIsStarted = false;
                speechRecognizer2.setRecognitionListener(new RecognitionListener() {
                    public void onRmsChanged(float f) {
                    }

                    private void restart() {
                        new Handler().postDelayed(new Runnable() {
                            public void run() {
                                SpeechControl.this.startSpeechRecognizerIntent();
                            }
                        }, 250);
                    }

                    public void onBeginningOfSpeech() {
                        if (!SpeechControl.this.speechRecognizerIsStarted) {
                        }
                    }

                    public void onBufferReceived(byte[] bArr) {
                        if (!SpeechControl.this.speechRecognizerIsStarted) {
                        }
                    }

                    public void onEndOfSpeech() {
                        if (SpeechControl.this.speechRecognizerIsStarted) {
                            restart();
                        }
                    }

                    public void onError(int i) {
                        if (SpeechControl.this.speechRecognizerIsStarted && i != 7) {
                            restart();
                        }
                    }

                    public void onEvent(int i, Bundle bundle) {
                        if (!SpeechControl.this.speechRecognizerIsStarted) {
                        }
                    }

                    public void onPartialResults(Bundle bundle) {
                        if (!SpeechControl.this.speechRecognizerIsStarted) {
                        }
                    }

                    public void onReadyForSpeech(Bundle bundle) {
                        if (!SpeechControl.this.speechRecognizerIsStarted) {
                        }
                    }

                    public void onResults(Bundle bundle) {
                        if (SpeechControl.this.speechRecognizerIsStarted) {
                            ArrayList stringArrayList = bundle.getStringArrayList("results_recognition");
                            int i = 0;
                            boolean z = false;
                            while (stringArrayList != null && i < stringArrayList.size()) {
                                if (((String) stringArrayList.get(i)).toLowerCase(Locale.US).contains("cheese")) {
                                    z = true;
                                }
                                i++;
                            }
                            if (z) {
                                SpeechControl.this.main_activity.audioTrigger();
                            } else if (stringArrayList != null && stringArrayList.size() > 0) {
                                StringBuilder sb = new StringBuilder();
                                sb.append((String) stringArrayList.get(0));
                                sb.append("?");
                                SpeechControl.this.main_activity.getPreview().showToast(SpeechControl.this.main_activity.getAudioControlToast(), sb.toString());
                            }
                        }
                    }
                });
                if (!this.main_activity.getMainUI().inImmersiveMode()) {
                    this.main_activity.findViewById(C0316R.C0318id.audio_control).setVisibility(0);
                }
            }
        } else if (this.speechRecognizer != null && !equals) {
            stopSpeechRecognizer();
        }
    }

    private void freeSpeechRecognizer() {
        this.speechRecognizer.cancel();
        try {
            this.speechRecognizer.destroy();
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "exception destroying speechRecognizer");
            e.printStackTrace();
        }
        this.speechRecognizer = null;
    }

    /* access modifiers changed from: 0000 */
    public void stopSpeechRecognizer() {
        if (this.speechRecognizer != null) {
            speechRecognizerStopped();
            this.main_activity.findViewById(C0316R.C0318id.audio_control).setVisibility(8);
            freeSpeechRecognizer();
        }
    }

    /* access modifiers changed from: 0000 */
    public boolean isStarted() {
        return this.speechRecognizerIsStarted;
    }

    /* access modifiers changed from: 0000 */
    public void stopListening() {
        this.speechRecognizer.stopListening();
        speechRecognizerStopped();
    }

    /* access modifiers changed from: 0000 */
    public boolean hasSpeechRecognition() {
        return this.speechRecognizer != null;
    }
}
