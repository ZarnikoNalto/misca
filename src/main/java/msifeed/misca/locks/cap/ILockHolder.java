package msifeed.misca.locks.cap;

import msifeed.misca.locks.LockType;
import msifeed.misca.locks.cap.lockable.ILockable;

public interface ILockHolder extends ILockable {
    boolean addLock(LockType type, int secret);
    boolean removeLock();
}
