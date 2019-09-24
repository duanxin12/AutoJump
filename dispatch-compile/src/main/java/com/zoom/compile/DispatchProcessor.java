package com.zoom.compile;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.zoom.annotation.AutoDispatch;
import com.zoom.annotation.DispatchMeta;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import static com.zoom.compile.Constant.INT_AUTO_DISPATCH;
import static com.zoom.compile.Constant.KEY_MODULE_NAME;
import static com.zoom.compile.Constant.LOAD_INTO_DISPATCHS;
import static com.zoom.compile.Constant.PACKAGE_OF_GENERATE_FILE;
import static com.zoom.compile.Constant.SEPARATOR;
import static javax.lang.model.element.Modifier.PUBLIC;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes(Constant.ANNOTATION_TYPE_DISPATCH)
public class DispatchProcessor extends AbstractProcessor {

    String moduleName = null;
    Elements elementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        elementUtils = processingEnv.getElementUtils();
        Map<String, String> options = processingEnv.getOptions();
        if (MapUtils.isNotEmpty(options)) {
            moduleName = options.get(KEY_MODULE_NAME);
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {

        if (CollectionUtils.isNotEmpty(annotations)) {

            Set<? extends Element> dispatchElements = roundEnvironment.getElementsAnnotatedWith(AutoDispatch.class);
            try {
                parseDispatchs(dispatchElements);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }

        return false;
    }

    private void parseDispatchs(Set<? extends Element> dispatchElements) throws IOException {
        if (CollectionUtils.isNotEmpty(dispatchElements)) {

            //Map<String, DispatchMeta>
            ParameterizedTypeName inputMapParamType = ParameterizedTypeName.get(
                    ClassName.get(Map.class),
                    ClassName.get(String.class),
                    ClassName.get(DispatchMeta.class));

            //Map<String, DispatchMeta> dispatchs
            ParameterSpec inputMapParamName = ParameterSpec.builder(inputMapParamType, "dispatchs").build();

            //public void loadIntoDispatchs(Map<String, DispatchMeta> dispatchs)
            MethodSpec.Builder loadIntoMethod = MethodSpec.methodBuilder(LOAD_INTO_DISPATCHS)
                    .addAnnotation(Override.class)
                    .addModifiers(PUBLIC)
                    .addParameter(inputMapParamName)
                    .returns(TypeName.VOID);


            //dispatchs.put($S, new DispatchMeta($T.class, $S)
            for (Element dispatch : dispatchElements) {
                String dispatchUrl = dispatch.getAnnotation(AutoDispatch.class).dispatch();
                //dispatchUrl /muduleName/url/...
                if (StringUtils.isEmpty(dispatchUrl) || !dispatchUrl.startsWith("/") || !(dispatchUrl.split("/").length > 1)) {
                    continue;
                }

                loadIntoMethod.addStatement("dispatchs.put($S, new DispatchMeta($T.class, $S))",
                        dispatchUrl,
                        ClassName.get(dispatch.getEnclosingElement().asType()),
                        dispatch.getSimpleName().toString());
            }

            //public class AutoDispatch$$ModuleName
            TypeSpec clazzType = TypeSpec.classBuilder("AutoDispatch"+ SEPARATOR + moduleName)
                    .addModifiers(PUBLIC)
                    .addMethod(loadIntoMethod.build())
                    .addSuperinterface(ClassName.get(elementUtils.getTypeElement(INT_AUTO_DISPATCH)))
                    .build();

            JavaFile javaFile = JavaFile.builder(PACKAGE_OF_GENERATE_FILE, clazzType).build();

            try {
                javaFile.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Set<String> getSupportedOptions() {
        return new HashSet<String>() {{
            this.add(KEY_MODULE_NAME);
        }};
    }
}
