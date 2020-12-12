package site.intunderflow.dscript.application.dapp_website;

import com.google.common.base.Preconditions;
import org.apache.commons.io.FilenameUtils;
import site.intunderflow.dscript.application.blocklattice.NetworkState;
import site.intunderflow.dscript.application.blocklattice.blockchain.address.BaseAddress;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.Block;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.CreateBlock;
import site.intunderflow.dscript.application.blocklattice.blockchain.block.DAppStateChangeBlock;
import site.intunderflow.dscript.application.blocklattice.nameservice.NameService;
import site.intunderflow.dscript.application.executor.ddl.v1.memory.ByteValue64;
import site.intunderflow.dscript.utility.Hex;
import spark.Request;
import spark.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DAppWebsiteServer {

    private final NetworkState networkState;

    private final NameService nameService;

    public DAppWebsiteServer(
            NetworkState networkState,
            NameService nameService
    ){
        this.networkState = Preconditions.checkNotNull(networkState);
        this.nameService = Preconditions.checkNotNull(nameService);
    }

    public void accept(Request request, Response response){
        System.out.println("Serving DApp");
        String host = request.host();
        StringBuilder subdomainBuilder = new StringBuilder();
        for (int i = 0; i < host.length(); i++){
            char c = host.charAt(i);
            if (c == '.'){
                break;
            }
            else{
                subdomainBuilder.append(c);
            }
        }
        String subdomain = subdomainBuilder.toString();
        BaseAddress address;
        if (subdomain.length() != 128){
            address = nameService.getForName(subdomain);
        }
        else {
            address = BaseAddress.fromString(subdomain);
        }
        if (address == null){
            response.status(404);
            response.body("No address found at the given name.");
            return;
        }
        Block createBlockRaw = networkState.getGenesis(address);
        if (createBlockRaw == null){
            response.status(404);
            response.body("No account found.");
            return;
        }
        CreateBlock createBlock = (CreateBlock) createBlockRaw;
        if (!createBlock.getAccountType().substring(0, 4).equals("dapp")){
            response.status(404);
            response.body("Account found, but not an application.");
            return;
        }
        Map<String, String> files = (Map<String, String>)createBlock.getInitializationParams().get("files");
        String path = request.pathInfo();
        if (path.equals("") || path.equals("/")){
            path = "index.html";
        }
        if (path.substring(0, 3).equals("/__")){
            Block latestBlock = networkState.get(
                    networkState.getHead(
                            address
                    )
            );
            Map<ByteValue64, ByteValue64> latestMemory;
            if (!latestBlock.isGenesis()){
                DAppStateChangeBlock stateChangeBlock = (DAppStateChangeBlock) latestBlock;
                latestMemory = stateChangeBlock.getPermanentMemory();
            }
            else{
                latestMemory = (Map<ByteValue64, ByteValue64>)createBlock.getInitializationParams().get("permanentMemory");
            }
            if (path.equals("/__permanentmemory_get")){
                String indexStr = request.queryParams("index");
                if (indexStr.length() != 128){
                    response.status(400);
                    response.body("Invalid length index, must be a hex encoded ByteValue64.");
                    return;
                }

                ByteValue64 index = new ByteValue64(
                        Hex.decode(
                                indexStr
                        )
                );
                ByteValue64 value = latestMemory.get(index);
                response.header("Content-Type", "text/plain");
                if (value == null){
                    response.status(200);
                    response.body("");
                    return;
                }
                else{
                    response.status(200);
                    response.body(Hex.encode(value.getValue().getArray()));
                    return;
                }
            }
            else if (path.equals("/__permanentmemory_list")){
                List<String> indexesList = new ArrayList<>();
                for (Map.Entry<ByteValue64, ByteValue64> entry : latestMemory.entrySet()){
                    indexesList.add(Hex.encode(entry.getKey().getValue().getArray()));
                }
                response.header("Content-Type", "text/plain");
                response.status(200);
                response.body(String.join(",", indexesList));
                return;
            }
            else{
                response.status(404);
                response.body("");
                return;
            }
        }
        String fileContent = files.get(path);
        if (fileContent == null){
            response.status(404);
            response.body("Application found, but no file at the given path.");
            return;
        }
        response.header("Content-Type", "text/" + FilenameUtils.getExtension(path));
        response.status(200);
        response.body(fileContent);
    }

}
