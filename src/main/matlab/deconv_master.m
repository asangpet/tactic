function [ out,err ] = deconv_master( blur, psf )
    len = max(size(blur));
    bias = 0.5;
    lucy = deconvlucy(blur,psf);
    [out,err] = findDeconvError(blur,psf,lucy,bias, blur, Inf);
    [out,err] = findDeconvError(blur,psf,circshift(lucy, [0, -len/2]), bias, out, err);
    
    reg = deconvreg(blur,psf);
    [out,err] = findDeconvError(blur,psf,reg, bias, out, err);
    [out,err] = findDeconvError(blur,psf,circshift(reg, [0, -len/2]), bias, out, err);    
end

