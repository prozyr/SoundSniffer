package com.example.soundsniffier;
import com.github.mikephil.charting.data.Entry;

import java.util.List;
public interface SoundDataObserver {
    void onDataReceived(List<Entry> entries, List<Entry> entries2);
}
