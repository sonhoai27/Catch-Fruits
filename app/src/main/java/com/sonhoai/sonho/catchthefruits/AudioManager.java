package com.sonhoai.sonho.catchthefruits;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Build;

public class AudioManager {
    public AudioAttributes audioAttributes;
    final int SOUND_POOL_MAX = 2;
    public static SoundPool soundPool;
    public static int hitSound;
    public static int overSound;
    public static int flySound;

    public AudioManager(Context context) {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();

            soundPool = new SoundPool.Builder()
                    .setAudioAttributes(audioAttributes)
                    .setMaxStreams(SOUND_POOL_MAX)
                    .build();
        }else{
            soundPool = new SoundPool(SOUND_POOL_MAX, android.media.AudioManager.STREAM_MUSIC, 0);
        }
        hitSound = soundPool.load(context, R.raw.hit, 1);
        overSound = soundPool.load(context, R.raw.over, 1);
        flySound = soundPool.load(context, R.raw.fly, 1);

    }

    public void playHitSound() {
        soundPool.play(hitSound, 1.0f, 1.0f, 1, 0, 1.0f);
    }

    public void  playOverSound(){
        soundPool.play(overSound, 1.0f, 1.0f, 1, 0, 1.0f);
    }

    public void  playFlyrSound(){
        soundPool.play(flySound, 1.0f, 1.0f, 1, 0, 1.0f);
    }
}
