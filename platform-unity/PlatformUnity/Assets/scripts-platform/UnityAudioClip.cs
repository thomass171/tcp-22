using System;
using System.IO;
using UnityEngine;
using java.lang;
using de.yard.threed.core.platform;
using de.yard.threed.core;
using de.yard.threed.core.resource;

namespace de.yard.threed.platform.unity
{
    public class UnityAudioClip : NativeAudioClip
    {
        static Log logger = PlatformUnity.getInstance().getLog(typeof(UnityAudioClip));
        public AudioClip clip;
        int position = 0;
        static int samplerate = 44100;
        float frequency = 440;

        public UnityAudioClip(NativeResource filename)
        {
            long starttime = Platform.getInstance().currentTimeMillis();
            logger.debug("Loading audio from " + filename.getFullName());

            //clip = AudioClip.Create("mySound", samplerate * 2, 1, samplerate, true, OnAudioRead, OnAudioSetPosition);
            // Resources.Load() works only with assets. There seems to be no easy way currently to load a clip from somewhere in the file system
            // maybe https://docs.unity3d.com/Manual/LoadingResourcesatRuntime.html
            clip = (AudioClip)Resources.Load(filename.getFullName());
            if (clip == null)
            {
                logger.error("Loading audio failed ");
            }

            //return new UnityTexture (loadImageFromFile (/*filename*/"/Users/thomas/Projekte/ThreeJs/images/Dangast.jpg"));
        }

        void OnAudioRead(float[] data)
        {
            logger.debug("Loaded audio bytes " + data.Length);
            /*int count = 0;
            while (count < data.Length)
            {
                data[count] = Mathf.Sin(2 * Mathf.PI * frequency * position / samplerate);
                position++;
                count++;
            }*/
        }

        void OnAudioSetPosition(int newPosition)
        {
            position = newPosition;
        }
    }
}
