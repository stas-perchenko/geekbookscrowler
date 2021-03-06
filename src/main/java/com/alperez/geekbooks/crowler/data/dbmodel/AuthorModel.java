package com.alperez.geekbooks.crowler.data.dbmodel;

import com.alperez.geekbooks.crowler.data.IdProvidingModel;
import com.alperez.geekbooks.crowler.data.LongId;
import com.alperez.geekbooks.crowler.utils.Nullable;
import com.alperez.geekbooks.crowler.utils.TextUtils;
import com.alperez.siphash.SipHash;
import com.alperez.siphash.SipHashKey;
import com.google.auto.value.AutoValue;

import java.io.UnsupportedEncodingException;

@AutoValue
public abstract class AuthorModel implements IdProvidingModel {
    @Nullable
    public abstract String name();
    public abstract String familyName();

    public String fullName() {
        return TextUtils.isEmpty(name()) ? familyName() : String.format("%s %s", name(), familyName());
    }

    private LongId<AuthorModel> id;

    public LongId<AuthorModel> id() {
        return id;
    }


    public static Builder builder() {
        return new AutoValue_AuthorModel.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder setName(@Nullable String name);
        public abstract Builder setFamilyName(String familyName);

        abstract AuthorModel actualBuild();

        public AuthorModel build() {
            AuthorModel instance = actualBuild();
            SipHashKey key = SipHashKey.ofBytes(new byte[]{-123, -82, 6, -17, -105, 33, 71, 113, -101, 121, -29, -71, 33, 107, 6, -97});
            try {
                long idValue = SipHash.calculateHash(key, String.format("%s:%s", instance.name(), instance.familyName()).getBytes("UTF-8")) >>> 1;
                instance.id = LongId.valueOf(idValue);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            return instance;
        }
    }
}
