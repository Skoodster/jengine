package main.lwjglb.engine.scene;
    import org.joml.Vector4f;
    import org.lwjgl.PointerBuffer;
    import org.lwjgl.assimp.*;
    import org.lwjgl.system.MemoryStack;

    import main.lwjglb.engine.graph.*;
    
    import java.io.File;
    import java.nio.IntBuffer;
    import java.util.*;

    import static org.lwjgl.assimp.Assimp.*;
    /**
     * aiProcesses:
     * JoinIntenticalVertices: reduces # of vertices by identifying which can be reused
     * Triangulate: use quads or other geometries to define elements. Since using triangles -> used to split faces into triangles
     * FixInfacingNormals: reverse normals that point inwards
     * CalcTangentSpace: use for implementing lights. Calcs tangent and bitangents using normals info
     * LimitBoneWeights: use for implementing anims. Limits the # of weights that affect a single vertex
     * PreTransformVertices: transforms over the loaded data so the model is placed in the origin and the coords are
     *                       corrected to math OpenGL coord system. Do not use this flag if model has anim. info will be removed
     */
public class ModelLoader {

    private ModelLoader(){
        // util class
    }

    // modelId: Identifier for the model to be loaded.
    // modelPath: Path to where the model file is located. A regular path, NOT a classpath because Assimp may need to load
    // additional files and may use the same base path as modelPath.
    // textureCache: Reference to the texture cache to avoid loading the same texture multiple times.
    public static Model loadModel(String modelId, String modelPath, TextureCache textureCache) {
        return loadModel(modelId, modelPath, textureCache, aiProcess_GenSmoothNormals | aiProcess_JoinIdenticalVertices |
        aiProcess_Triangulate | aiProcess_FixInfacingNormals | aiProcess_CalcTangentSpace | aiProcess_LimitBoneWeights |
        aiProcess_PreTransformVertices);
    }

    public static Model loadModel(String modelId, String modelPath, TextureCache textureCache, int flags){
        File file = new File(modelPath);
        if (!file.exists()){
            throw new RuntimeException("Model path does not exist [" + modelPath+"]");
        }
        String modelDir = file.getParent();

        AIScene aiScene = aiImportFile(modelPath, flags);
        if (aiScene == null){
            throw new RuntimeException("Error loading model [modelPath: "+modelPath+"]");
        }

        // Process materials contained in the model
        int numMaterials = aiScene.mNumMaterials();
        List<Material> materialList = new ArrayList<>();
        for (int i = 0; i < numMaterials; i++){
            AIMaterial aiMaterial = AIMaterial.create(aiScene.mMaterials().get(i));
            materialList.add(processMaterial(aiMaterial, modelDir, textureCache));
        }

        // Process meshes contained in model
        // Process meshes after materials to link them bc a model can define several meshes and each of them can use one of the
        // materials defined for the model.
        int numMeshes = aiScene.mNumMeshes();
        // buffer is like C pointers -> point to a memory region. Need to know the type of data before hand to process them
        PointerBuffer aiMeshes = aiScene.mMeshes();
        Material defaultMaterial = new Material();
        for (int i=0; i < numMeshes; i++){
            AIMesh aiMesh = AIMesh.create(aiMeshes.get(i));
            Mesh mesh = processMesh(aiMesh);
            int materialIdx = aiMesh.mMaterialIndex();
            Material material;
            if (materialIdx >= 0 && materialIdx < materialList.size()){
                material = materialList.get(materialIdx);
            } else {
                material = defaultMaterial;
            }
            material.getMeshList().add(mesh);
        }
        
        if (!defaultMaterial.getMeshList().isEmpty()){
            materialList.add(defaultMaterial);
        }

        return new Model(modelId, materialList);
    }

    private static Material processMaterial(AIMaterial aiMaterial, String modelDir, TextureCache textureCache){
        Material material = new Material();
        try (MemoryStack stack = MemoryStack.stackPush()){
            AIColor4D color = AIColor4D.create();

            // Get material color
            int result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_DIFFUSE, aiTextureType_NONE,0,color);
            if (result == aiReturn_SUCCESS){
                material.setDiffuseColor(new Vector4f(color.r(), color.g(), color.b(), color.a()));
            }

            AIString aiTexturePath = AIString.calloc(stack);
            // Check if material defines a texture
            // If true -> Set diffuse color to a default value (black)
            // If false -> Use a default black texture which can be combined with the material color
            aiGetMaterialTexture(aiMaterial, aiTextureType_DIFFUSE, 0, aiTexturePath, (IntBuffer) null, null, null, null, null, null);
            String texturePath = aiTexturePath.dataString();
            if (texturePath != null && texturePath.length() > 0){
                material.setTexturePath(modelDir + File.separator + new File(texturePath).getName());
                textureCache.createTexture(material.getTexturePath());
                material.setDiffuseColor(Material.DEFAULT_COLOR);
            }
            return material;
        }
    }

    private static Mesh processMesh(AIMesh aiMesh){
        float[] vertices = processVertices(aiMesh);
        float[] textCoords = processTextCoords(aiMesh);
        int[] indices = processIndices(aiMesh);

        // Texture coords may not have been populated. Need empty slots
        if (textCoords.length == 0){
            int numElements = (vertices.length/3) *2;
            textCoords = new float[numElements];
        }
        return new Mesh(vertices, textCoords, indices);
    }

    private static int[] processIndices(AIMesh aiMesh){
        List<Integer> indices = new ArrayList<>();
        int numFaces = aiMesh.mNumFaces();
        AIFace.Buffer aiFaces = aiMesh.mFaces();
        for (int i =0; i <numFaces; i++){
            AIFace aiFace = aiFaces.get(i);
            IntBuffer buffer = aiFace.mIndices();
            while (buffer.remaining() > 0){
                indices.add(buffer.get());
            }
        }
        return indices.stream().mapToInt(Integer::intValue).toArray();
    }

    private static float[] processTextCoords(AIMesh aiMesh){
        AIVector3D.Buffer buffer = aiMesh.mTextureCoords(0);
        if (buffer == null){
            return new float[]{};
        }
        float[] data = new float[buffer.remaining() * 2];
        int pos = 0;
        while (buffer.remaining() > 0){
            AIVector3D textCoord = buffer.get();
            data[pos++] = textCoord.x();
            data[pos++] = 1 - textCoord.y();
        }
        return data;
    }

    private static float[] processVertices(AIMesh aiMesh){
        AIVector3D.Buffer buffer = aiMesh.mVertices();
        float[] data = new float[buffer.remaining() * 3];
        int pos = 0;
        while (buffer.remaining() > 0){
            AIVector3D textCoord = buffer.get();
            data[pos++] = textCoord.x();
            data[pos++] = textCoord.y();
            data[pos++] = textCoord.z();
        }
        return data;
    }
}
