package msifeed.misca.charstate.cap;

import msifeed.misca.charsheet.CharNeed;
import msifeed.sys.cap.FloatContainer;

public class CharstateImpl implements ICharstate {
    private long updateTime;
    private long miningTime;
    private long silenceTime;
    private int nonce;

    private final FloatContainer<CharNeed> tolerances = new FloatContainer<>(CharNeed.class, 0, 0, 1);

    @Override
    public long getUpdateTime() {
        return updateTime;
    }

    @Override
    public void setUpdateTime(long value) {
        this.updateTime = value;
    }

    @Override
    public long getMiningTime() {
        return miningTime;
    }

    @Override
    public void setMiningTime(long value) {
        this.miningTime = value;
    }

    @Override
    public long getSilenceTime() {
        return silenceTime;
    }

    @Override
    public void setSilenceTime(long value) {
        this.silenceTime = value;
    }

    @Override
    public int nonce() {
        return nonce;
    }

    @Override
    public void incNonce() {
        nonce++;
    }

    @Override
    public void setNonce(int nonce) {
        this.nonce = nonce;
    }

    @Override
    public FloatContainer<CharNeed> tolerances() {
        return tolerances;
    }

    @Override
    public void replaceWith(ICharstate other) {
        updateTime = other.getUpdateTime();
        miningTime = other.getMiningTime();
        silenceTime = other.getSilenceTime();
        nonce = other.nonce();
        tolerances.replaceWith(other.tolerances());
    }
}
