using System;
using System.IO;
using UnityEngine;
using java.lang;
using de.yard.threed.core.platform;
using de.yard.threed.core;
using de.yard.threed.core.resource;

namespace de.yard.threed.platform.unity
{

    public class UnityAudio : NativeAudio
    {
        static Log logger = PlatformUnity.getInstance().getLog(typeof(UnityAudio));
        public AudioSource audioSource;

        public UnityAudio(UnityAudioClip audioClip)
        {
            GameObject empty = new GameObject();

            audioSource = empty.AddComponent<AudioSource>();

        }


        public void setVolume(double v)
        {
            //audioNode.setVolume((float)v);
        }

        public void play()
        {
            logger.debug("Playing audio");
            audioSource.volume = 1.0f;
            audioSource.Play();
        }

        public void setLooping(bool b)
        {
            //audioNode.setLooping(b);
        }
    }
}
