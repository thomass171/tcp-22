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
            clip = AudioClip.Create(filename.getFullName(), samplerate * 2, 1, samplerate, true, OnAudioRead, OnAudioSetPosition);

            //return new UnityTexture (loadImageFromFile (/*filename*/"/Users/thomas/Projekte/ThreeJs/images/Dangast.jpg"));
        }

        void OnAudioRead(float[] data)
        {
            int count = 0;
            while (count < data.Length)
            {
                data[count] = Mathf.Sin(2 * Mathf.PI * frequency * position / samplerate);
                position++;
                count++;
            }
        }

        void OnAudioSetPosition(int newPosition)
        {
            position = newPosition;
        }
    }
}
