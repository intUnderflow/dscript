package site.intunderflow.dscript.application.blocklattice.blockchain.address;

import com.google.common.base.Preconditions;
import site.intunderflow.dscript.application.blocklattice.blockchain.BlockHashFactory;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.CreateBlock;
import site.intunderflow.dscript.utility.FixedMaxLengthString;
import site.intunderflow.dscript.utility.Hex;

public class BaseAddress {

    private final FixedMaxLengthString address;

    public BaseAddress(
            byte[] address
    ){
        this(Hex.encode(address));
    }

    public BaseAddress(
            String address
    ){
        this.address = new FixedMaxLengthString(128, Preconditions.checkNotNull(address));
        Preconditions.checkArgument(addressValid(address), "Address not valid.");
    }

    private boolean addressValid(String toCheck){
        return toCheck.length() == 128 && toCheck.matches("-?[0-9a-fA-F]+");
    }

    public String getAddress(){
        return address.getValue();
    }

    public byte[] toBytes(){ return Hex.decode(address.getValue()); }

    @Override
    public String toString(){
        return getAddress();
    }

    @Override
    public boolean equals(Object o){
        if (o == null){
            return false;
        }
        else if (!o.getClass().equals(getClass())){
            return false;
        }
        else {
            return toString().equals(o.toString());
        }
    }

    @Override
    public int hashCode(){
        return toString().hashCode();
    }

    public static BaseAddress forCreateBlock(CreateBlock createBlock){
        return new BaseAddress(new BlockHashFactory(createBlock).hash());
    }

    public static BaseAddress fromString(String from){
        return new BaseAddress(from);
    }
}
