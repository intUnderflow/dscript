package site.intunderflow.dscript.utility.crypto.keys;

import com.google.crypto.tink.KeysetHandle;

abstract class KeysetContainer {

    abstract KeysetHandle getKeysetHandle();

}
