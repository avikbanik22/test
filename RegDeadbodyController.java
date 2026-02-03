/*
 * Copyright 2011 National Crime Records Bureau. All rights reserved.
 */
package org.cctns.cas.state.online.registration.spring;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.jasperreports.engine.JRException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.fileupload.FileItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.cctns.cas.state.online.common.constant.CommonConstants;
import org.cctns.cas.state.online.common.constant.MailConstants;
import org.cctns.cas.state.online.common.constant.ReportsConstants;
import org.cctns.cas.state.online.common.delegate.DocumentBusinessDelegate;
import org.cctns.cas.state.online.common.service.IPhysicalFeaturesService;
import org.cctns.cas.state.online.common.service.MailService;
import org.cctns.cas.state.online.common.util.BeanNameResolver;
import org.cctns.cas.state.online.common.util.EnvNameResolver;
import org.cctns.cas.state.online.common.util.ErrorList;
import org.cctns.cas.state.online.common.util.MailUtil;
import org.cctns.cas.state.online.common.util.MessageSelector;
import org.cctns.cas.state.online.common.util.ResourceUtil;
import org.cctns.cas.state.online.common.util.StrUtil;
import org.cctns.cas.state.online.common.vo.EmailAlertBean;
import org.cctns.cas.state.online.common.vo.User;
import org.cctns.cas.state.online.registration.constant.RegistrationConstants;
import org.cctns.cas.state.online.common.delegate.DelegateWrapper;
import org.cctns.cas.state.online.common.exception.ApplicationException;
import org.cctns.cas.state.online.common.util.FileConversionUtil;
import org.cctns.cas.state.online.common.util.JasperReportsUtil;
import org.cctns.cas.state.online.common.vo.FileUploadDownloadBean;
import org.cctns.cas.state.online.registration.delegate.GdEntryBusinessDelegate;
import org.cctns.cas.state.online.registration.delegate.RegDeadbodyBusinessDelegate;
import org.cctns.cas.state.online.registration.delegate.RegistrationFirBusinessDelegate;
import org.cctns.cas.state.online.registration.exception.RegDeadbodyException;
import org.cctns.cas.state.online.registration.validator.DeadBodyAddEnquiryValidator;
import org.cctns.cas.state.online.registration.validator.DeadBodyAddPanchnamaValidator;
import org.cctns.cas.state.online.registration.validator.DeadBodyAddPostMortemValidator;
import org.cctns.cas.state.online.registration.validator.DeadBodyChangeEOValidator;
import org.cctns.cas.state.online.registration.validator.DeadBodyPreparePostMortemValidator;
import org.cctns.cas.state.online.registration.validator.DeadBodyWaivePostMortemValidator;
import org.cctns.cas.state.online.registration.validator.DeadbodyValidator;
import org.cctns.cas.state.online.registration.validator.FileUploadValidator;
import org.cctns.cas.state.online.registration.vo.AddPostMortReportFormBean;
import org.cctns.cas.state.online.registration.vo.DeadBodyFilesFormBean;
import org.cctns.cas.state.online.registration.vo.PreparePostMortReqFormBean;
import org.cctns.cas.state.online.registration.vo.TDeadBodyRegistrationAddEnquiryFormBean;
import org.cctns.cas.state.online.registration.vo.TDeadBodyRegistrationApprovePostmorFormBean;
import org.cctns.cas.state.online.registration.vo.TDeadBodyRegistrationChangeEnquiryFormBean;
import org.cctns.cas.state.online.registration.vo.TDeadBodyRegistrationFormBean;
import org.cctns.cas.state.online.registration.vo.TDeadBodyRegistrationPanchnamaFormBean;
import org.cctns.cas.state.online.investigation.delegate.ProclaimedOffenderBusinessDelegate;
import org.cctns.cas.state.online.registration.vo.NationalityVO;
import org.springframework.beans.factory.support.ManagedMap;

//******* START -- MedLEAPER -- SUMIT SAHA -- MARCH 2025 ********
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import org.cctns.cas.state.online.registration.vo.PMRMedLeaperVO;
import org.cctns.cas.state.online.registration.delegate.RegistrationMlcBusinessDelegate;

//******* END -- MedLEAPER -- SUMIT SAHA -- MARCH 2025 ********

import net.sf.json.JSON;
import javax.ws.rs.core.Response;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.logging.Level;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jsse.provider.BouncyCastleJsseProvider;
import org.springframework.util.FileCopyUtils;

/**
 *
 * Copyright: NCRB. Project Name: CCTNS Class Name:RegDeadbodyController.java
 * Description: Controller for Unidentified DeadBody/Unnatural Death Version
 * Information: 2.0, Sandeep,Vikram,Hussain,Srinivas,Chithra, 01-08-11
 * Modification History: V2.1, Sandeep T.P, 15/10/2012 1. Provided comments for
 * methods V2.2, Sandeep T.P, 16/10/2012 1. Removed Unused methods V2.3 Soumya R
 * , 16/07/2013 1. Modified for implementing history of previous enquiry report
 * V2.4 Soumya R , 17/07/2013 1. Modified for inserting inquest num & dead body
 * person code for sending alert while matching V2.5, Soumya,18/07/2013 Modified
 * for Changing source of information field to selection V2.6, Soumya R
 * 21/08/2013 1.Modified for displaying four digit uidb inquest number in view
 * V2.7, Soumya R, 24/01/2014 Modified for Code Clean Up V 2.8 Soumya R,
 * 26/02/2014 Modified for correcting code after server side validation V 3.1,
 * Soumya R, 10/03/2014 1.Modified for Pending Task Implementation of UIDB V
 * 3.2, Soumya R, 27/01/2015 1.Modified display format for Enquiry Officer name.
 */
@Controller
public class RegDeadbodyController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegDeadbodyController.class);
    @Autowired
    private ProclaimedOffenderBusinessDelegate proclaimedOffenderBusinessDelegate;
    @Autowired
    private RegDeadbodyBusinessDelegate deadbodybusinessDelegate;
    @Autowired
    private DocumentBusinessDelegate documentBusinessDelegate;
    @Autowired
    private RegistrationFirBusinessDelegate registrationFirBusinessDelegate;
    @Autowired
    MailService mailService;
    @Autowired
    MailUtil mailUtil;
    @Autowired
    private IPhysicalFeaturesService iPhysicalFeaturesService;
    private DeadbodyValidator dbvalidator = new DeadbodyValidator();
    private DeadBodyChangeEOValidator deadBodyChangeEOValidator = new DeadBodyChangeEOValidator();
    private DeadBodyAddEnquiryValidator deadBodyAddEnquiryValidator = new DeadBodyAddEnquiryValidator();
    private DeadBodyAddPanchnamaValidator deadBodyAddPanchnamaValidator = new DeadBodyAddPanchnamaValidator();
    private DeadBodyWaivePostMortemValidator deadBodyRegWaiveValidator = new DeadBodyWaivePostMortemValidator();
    private DeadBodyPreparePostMortemValidator deadBodyPreparePostValidator = new DeadBodyPreparePostMortemValidator();
    private DeadBodyAddPostMortemValidator deadBodyAddPostValidator = new DeadBodyAddPostMortemValidator();
    private static final Logger LOG = LoggerFactory.getLogger(RegistrationFirController.class);
    @Autowired
    private JasperReportsUtil jasperReportsUtil;
    private static final String USER_BEAN = "USER_BEAN";
    @Autowired
    ServletContext servletContext;
    @Autowired
    private GdEntryBusinessDelegate businessDelegate;
//    ******* START -- MedLEAPER -- SUMIT SAHA -- MARCH 2025 ********
    @Autowired
    private RegistrationMlcBusinessDelegate registrationMlcBusinessDelegate;

//******* END -- MedLEAPER -- SUMIT SAHA -- MARCH 2025 ********
    /**
     * Default constructor
     */
    public RegDeadbodyController() {
    }

    /**
     * setter method for Jasper Reports Util
     *
     * @param jasperReportsUtil JasperReportsUtil object
     */
    public void setJasperReportsUtil(JasperReportsUtil jasperReportsUtil) {
        this.jasperReportsUtil = jasperReportsUtil;
    }

    /**
     * get DeadBodyPreparePostValidator
     *
     * @return
     */
    public DeadBodyPreparePostMortemValidator getDeadBodyPreparePostValidator() {
        return deadBodyPreparePostValidator;
    }

    /**
     * set DeadBodyPreparePostValidator
     *
     * @param deadBodyPreparePostValidator DeadBodyPreparePostMortemValidator
     */
    public void setDeadBodyPreparePostValidator(DeadBodyPreparePostMortemValidator deadBodyPreparePostValidator) {
        this.deadBodyPreparePostValidator = deadBodyPreparePostValidator;
    }

    /**
     * get DeadBodyRegWaiveValidator
     *
     * @return
     */
    public DeadBodyWaivePostMortemValidator getDeadBodyRegWaiveValidator() {
        return deadBodyRegWaiveValidator;
    }

    /**
     * set DeadBodyRegWaiveValidator
     *
     * @param deadBodyRegWaiveValidator DeadBodyWaivePostMortemValidator
     */
    public void setDeadBodyRegWaiveValidator(DeadBodyWaivePostMortemValidator deadBodyRegWaiveValidator) {
        this.deadBodyRegWaiveValidator = deadBodyRegWaiveValidator;
    }

    /**
     * get DeadBodyAddPanchnamaValidator
     *
     * @return
     */
    public DeadBodyAddPanchnamaValidator getDeadBodyAddPanchnamaValidator() {
        return deadBodyAddPanchnamaValidator;
    }

    /**
     * set DeadBodyAddPanchnamaValidator
     *
     * @param deadBodyAddPanchnamaValidator DeadBodyAddPanchnamaValidator
     */
    public void setDeadBodyAddPanchnamaValidator(DeadBodyAddPanchnamaValidator deadBodyAddPanchnamaValidator) {
        this.deadBodyAddPanchnamaValidator = deadBodyAddPanchnamaValidator;
    }

    /**
     * get DeadBodyAddEnquiryValidator
     *
     * @return
     */
    public DeadBodyAddEnquiryValidator getDeadBodyAddEnquiryValidator() {
        return deadBodyAddEnquiryValidator;
    }

    /**
     * set DeadBodyAddEnquiryValidator
     *
     * @param deadBodyAddEnquiryValidator DeadBodyAddEnquiryValidator
     */
    public void setDeadBodyAddEnquiryValidator(DeadBodyAddEnquiryValidator deadBodyAddEnquiryValidator) {
        this.deadBodyAddEnquiryValidator = deadBodyAddEnquiryValidator;
    }

    /**
     * get DeadBodyChangeEOValidator
     *
     * @return
     */
    public DeadBodyChangeEOValidator getDeadBodyChangeEOValidator() {
        return deadBodyChangeEOValidator;
    }

    /**
     * set DeadBodyChangeEOValidator
     *
     * @param deadBodyChangeEOValidator DeadBodyChangeEOValidator
     */
    public void setDeadBodyChangeEOValidator(DeadBodyChangeEOValidator deadBodyChangeEOValidator) {
        this.deadBodyChangeEOValidator = deadBodyChangeEOValidator;
    }

    /**
     * get DocumentBusinessDelegate
     *
     * @return
     */
    public DocumentBusinessDelegate getDocumentBusinessDelegate() {
        return documentBusinessDelegate;
    }

    /**
     * set DocumentBusinessDelegate
     *
     * @param documentBusinessDelegate DocumentBusinessDelegate
     */
    public void setDocumentBusinessDelegate(DocumentBusinessDelegate documentBusinessDelegate) {
        this.documentBusinessDelegate = documentBusinessDelegate;
    }

    /**
     * get PhysicalFeaturesService
     *
     * @return
     */
    public IPhysicalFeaturesService getiPhysicalFeaturesService() {
        return iPhysicalFeaturesService;
    }

    /**
     * set PhysicalFeaturesService
     *
     * @param iPhysicalFeaturesService IPhysicalFeaturesService
     */
    public void setiPhysicalFeaturesService(IPhysicalFeaturesService iPhysicalFeaturesService) {
        this.iPhysicalFeaturesService = iPhysicalFeaturesService;
    }

    /**
     * get Dbvalidator
     *
     * @return
     */
    public DeadbodyValidator getDbvalidator() {
        return dbvalidator;
    }

    /**
     * set Dbvalidator
     *
     * @param dbvalidator DeadbodyValidator
     */
    public void setDbvalidator(DeadbodyValidator dbvalidator) {
        this.dbvalidator = dbvalidator;
    }
    private FileUploadValidator uploadValidator = new FileUploadValidator();

    /**
     * get the uploadValidator
     *
     * @return uploadValidator
     */
    public FileUploadValidator getUploadValidator() {
        return uploadValidator;
    }

    /**
     * set the uploadValidator
     *
     * @param uploadValidator FileUploadValidator
     */
    public void setUploadValidator(final FileUploadValidator uploadValidator) {
        this.uploadValidator = uploadValidator;
    }

    /**
     * get the deadbodybusinessDelegate
     *
     * @return deadbodybusinessDelegate
     */
    public RegDeadbodyBusinessDelegate getDeadbodybusinessDelegate() {
        return deadbodybusinessDelegate;
    }

    /**
     * set the deadbodybusinessDelegate
     *
     * @param deadbodybusinessDelegate RegDeadbodyBusinessDelegate
     */
    public void setDeadbodybusinessDelegate(final RegDeadbodyBusinessDelegate deadbodybusinessDelegate) {
        this.deadbodybusinessDelegate = deadbodybusinessDelegate;
    }

    public RegistrationFirBusinessDelegate getRegistrationFirBusinessDelegate() {
        return registrationFirBusinessDelegate;
    }

    public void setRegistrationFirBusinessDelegate(RegistrationFirBusinessDelegate registrationFirBusinessDelegate) {
        this.registrationFirBusinessDelegate = registrationFirBusinessDelegate;
    }

    /**
     * This method gets the dead body registration screen regdeadbody.htm on
     * load
     *
     * @param model ModelMap
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param session HttpSession
     * @return model
     * @throws ParseException
     */
    @RequestMapping(value = "/regdeadbody.htm", method = RequestMethod.GET)
    public ModelAndView showRegDeadbody(final ModelMap model, final HttpSession session, final HttpServletRequest request) throws ParseException {
        clearSessionAttributesDeadBody(session);
        TDeadBodyRegistrationFormBean regDeadbodyFormBean = new TDeadBodyRegistrationFormBean();
        final User user = (User) session.getAttribute(BeanNameResolver.getBeanName("USER_BEAN"));
        try {

            //    final String inquestNoFromExisting = session.getAttribute("existingInquestNo").toString();
//            if (session.getAttribute("existingInquestNo") != null) {
//                 final String inquestNoFromExisting = session.getAttribute("existingInquestNo").toString();
//                regDeadbodyFormBean = deadbodybusinessDelegate.getExistingUidbDetails(inquestNoFromExisting, user);
//            }
            regDeadbodyFormBean.setRegState(user.getStateWithLang());
            regDeadbodyFormBean.setRegDistrict(user.getDistrictWithLang());
            regDeadbodyFormBean.setRegPs(user.getPsWithLang());
            regDeadbodyFormBean.setInformerDistrictCd(user.getDistrictCd().toString());
            regDeadbodyFormBean.setInformerPsCd(user.getPoliceStationCd().toString());
            regDeadbodyFormBean.setInformerStateCd(user.getStateCd().toString());
            regDeadbodyFormBean.setInformerPermDistrictEng(user.getDistrictCd().toString());
            regDeadbodyFormBean.setInformerPermPsEng(user.getPoliceStationCd().toString());
            regDeadbodyFormBean.setInformerPermStateEng(user.getStateCd().toString());
            regDeadbodyFormBean.setDeceasedDistrictEng(user.getDistrictCd().toString());
            regDeadbodyFormBean.setDeceasedPsEng(user.getPoliceStationCd().toString());
            regDeadbodyFormBean.setDeceasedStateEng(user.getStateCd().toString());
            regDeadbodyFormBean.setDeceasedPermDistrictEng(user.getDistrictCd().toString());
            regDeadbodyFormBean.setDeceasedPermPsEng(user.getPoliceStationCd().toString());
            regDeadbodyFormBean.setDeceasedPermStateEng(user.getStateCd().toString());
            regDeadbodyFormBean.setIdentifierDistrictEng(user.getDistrictCd().toString());
            regDeadbodyFormBean.setIdentifierPsEng(user.getPoliceStationCd().toString());
            regDeadbodyFormBean.setIdentifierStateEng(user.getStateCd().toString());
            regDeadbodyFormBean.setIdentifierPermDistrictEng(user.getDistrictCd().toString());
            regDeadbodyFormBean.setIdentifierPermPsEng(user.getPoliceStationCd().toString());
            regDeadbodyFormBean.setIdentifierPermStateEng(user.getStateCd().toString());
            populateSelectionFieldsData(model, session);
            regDeadbodyFormBean.setOriginalRecord(1);
            String hiddenDeathDate = regDeadbodyFormBean.getDeathDt();
            session.removeAttribute("existingInquestNo");
            model.addAttribute("hiddenDeathDate", hiddenDeathDate);
            model.addAttribute("user", user);
            model.addAttribute("regdeadbody", regDeadbodyFormBean);

        } finally {
            regDeadbodyFormBean = null;
        }
        return new ModelAndView("regdeadbody", model);
    }

    /**
     * This method submits the dead body registration screen regdeadbody.htm
     *
     * @param regDeadbodyFormBean TDeadBodyRegistrationFormBean
     * @param model ModelMap
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @return model
     * @throws Exception
     *
     */
    @RequestMapping(value = "/regdeadbody.htm", method = RequestMethod.POST)
    protected ModelAndView onSubmit(@ModelAttribute("regdeadbody") final TDeadBodyRegistrationFormBean regDeadbodyFormBean,
            BindingResult result, final HttpServletRequest request, final HttpSession session, ModelMap model, final ErrorList errorList) throws ApplicationException {

        DelegateWrapper wrapper = new DelegateWrapper();
        ModelAndView modelAndView = null;
        ArrayList FileList = (ArrayList) request.getAttribute("FileList");
        String inquestNumber = (String) session.getAttribute("inquestNo");
        if (inquestNumber != null && !inquestNumber.equals("")) {
            regDeadbodyFormBean.setInquestNumber(inquestNumber);
        }
        try {
            if (!FileList.isEmpty()) {
                regDeadbodyFormBean.setFileList(FileList);
            }
            final ArrayList idTypeList = (ArrayList) session.getAttribute(RegistrationConstants.REG_DEADBODY_NATIONALITY);
            wrapper.addListFormBean(RegistrationConstants.REG_DEADBODY_NATIONALITY, idTypeList);
            final ArrayList decidTypeList = (ArrayList) session.getAttribute(RegistrationConstants.REG_DEADBODY_NATIONALITY1);
            wrapper.addListFormBean(RegistrationConstants.REG_DEADBODY_NATIONALITY1, decidTypeList);
            LOGGER.debug("Preparing to check server side validation for registration of deadbody");
            dbvalidator.validate(regDeadbodyFormBean, result);
            if (result.hasErrors()) {
                populateSelectionFieldsData(model, session);
                model.addAttribute("decidTypeList", decidTypeList);
                model.addAttribute("idTypeList", idTypeList);
                model.addAttribute("regdeadbody", regDeadbodyFormBean);
                modelAndView = new ModelAndView("regdeadbody", model);
            } else {
                LOGGER.debug("Preparing to sumbit registration of deadbody");
                modelAndView = registerDeadbody(session, regDeadbodyFormBean, request);
            }
        } finally {
            wrapper = null;
        }
        return modelAndView;

    }

    /**
     *
     * Method for submission of uidb
     *
     * @param session
     * @param regDeadbodyFormBean
     * @param request
     * @return
     * @throws ApplicationException
     */
    private ModelAndView registerDeadbody(HttpSession session, final TDeadBodyRegistrationFormBean regDeadbodyFormBean, final HttpServletRequest request) throws ApplicationException {

        User user = (User) session.getAttribute(BeanNameResolver.getBeanName("USER_BEAN"));
        ErrorList errorList = new ErrorList();
        ModelMap model = new ModelMap();
        regDeadbodyFormBean.setLangCd(user.getLangCd());
        DelegateWrapper wrapper = new DelegateWrapper();
        String errorMsg = "";
        try {
            final ArrayList deadbodyNationality = (ArrayList) session.getAttribute(RegistrationConstants.REG_DEADBODY_NATIONALITY);
            final ArrayList deadbodyNationality1 = (ArrayList) session.getAttribute(RegistrationConstants.REG_DEADBODY_NATIONALITY1);
            List<NationalityVO> nationalityList = (List<NationalityVO>) session.getAttribute("addNationalityFor");
            wrapper.addFormBean(RegistrationConstants.REG_DEADBODY_FORM_BEAN_ID, regDeadbodyFormBean);
            wrapper.addListFormBean(RegistrationConstants.REG_DEADBODY_NATIONALITY, deadbodyNationality);
            wrapper.addListFormBean(RegistrationConstants.REG_DEADBODY_NATIONALITY1, deadbodyNationality1);
            LOGGER.debug("Preparing to save registration of deadbody");
            final Long inquestNo = deadbodybusinessDelegate.saveDeadbody(regDeadbodyFormBean, wrapper, errorList, user, deadbodyNationality, deadbodyNationality1);
            if (errorList.hasErrors()) {
                for (String errCode : errorList.getErrors()) {
                    errorMsg = ResourceUtil.getCommonMessage(errCode, user.getLangCd());
                    request.setAttribute("errorMsg", errorMsg);
                }
                session.removeAttribute(RegistrationConstants.REG_DEADBODY_NATIONALITY);
                session.removeAttribute(RegistrationConstants.REG_DEADBODY_NATIONALITY1);
                return new ModelAndView(new RedirectView("regdeadbody.htm?save=failure&message=" + errorMsg));
            } else {
                regDeadbodyFormBean.setInquestNum(inquestNo.toString());
                final String inqNo = inquestNo.toString();
                final String shortInqNo = inqNo.substring(inqNo.length() - 4);
                LOGGER.debug("Preparing to save matching details for inquest num :" + inquestNo);
                final List matchResultList = deadbodybusinessDelegate.matchFoundPerson(regDeadbodyFormBean, user);
                request.setAttribute("inquestNo", inquestNo + "");

                session.removeAttribute(RegistrationConstants.REG_DEADBODY_NATIONALITY);
                session.removeAttribute(RegistrationConstants.REG_DEADBODY_NATIONALITY1);
                sendEMail(regDeadbodyFormBean, user.getLangCd(), user);
                session.setAttribute("dbInquestNo", inquestNo);
                session.setAttribute("bodyTypeCd", regDeadbodyFormBean.getBodyTypeCd());
                session.setAttribute("deathTypeCd", regDeadbodyFormBean.getIsDbIdentified());

                session.setAttribute(CommonConstants.MESSAGE_DEAD, shortInqNo);
                session.setAttribute(CommonConstants.MESSAGE_CODE_SUCCESS, true);
                model.addAttribute("matchResultList", matchResultList);
                model.addAttribute("foundpersonregistration", regDeadbodyFormBean);
                return new ModelAndView("unidentifiedFoundMatchDeadBody", model);
            }
        } finally {
            errorList = null;
            model = null;
            wrapper = null;
        }
    }

    /**
     * <p>send EMail</p>
     *
     * @param regDeadbodyFormBean
     * @param langCd
     * @param user
     * @throws ApplicationException
     */
    public void sendEMail(TDeadBodyRegistrationFormBean regDeadbodyFormBean, Integer langCd, User user) throws ApplicationException {
        int emailTypeCd = MailConstants.DEADBODY_REGISTRATION;
        EmailAlertBean emailAlert = mailUtil.getEmailAlert(emailTypeCd);
        String systemEmailAddress = EnvNameResolver.envMap("mail.system.emailaddress");
        String emailName = emailAlert.getEmailName();
        String emailDesc = emailAlert.getEmailDesc();
        String emailid = EnvNameResolver.envMap("mail.dept.uidb.nationalhumanrightscommision");
        String emailidcc = EnvNameResolver.envMap("mail.dept.uidb.statehumanrightscommision");
        emailDesc = emailDesc.replace("$ps", user.getPoliceStation());
        emailDesc = emailDesc.replace("$regno", regDeadbodyFormBean.getInquestNum());
        mailService.sendMail(user, emailTypeCd, systemEmailAddress, emailid, emailidcc, emailName, emailDesc);
    }

    /**
     * <p>Controller method For Search Page Request Gets the default ten values
     * on page load of mostrecentdeadbodyregistrations.htm </p>
     *
     * @param model ModelMap
     * @param request HttpServletRequest
     * @param session HttpSession
     * @return model
     */
    @RequestMapping(value = "/mostrecentdeadbodyregistrations.htm", method = RequestMethod.GET)
    public ModelAndView showDeadBodySearch(final ModelMap model, final HttpServletRequest request, final HttpSession session) {

        TDeadBodyRegistrationFormBean regDeadbodyFormBean = new TDeadBodyRegistrationFormBean();
        try {
            final String target = request.getParameter("target");
            LOGGER.debug("Preparing to load search page for uidb");
            model.addAttribute("mostrecentdeadbodyregistrations", regDeadbodyFormBean);
            LOGGER.info("The values of form bean are" + regDeadbodyFormBean.getInquestNum());
            model.addAttribute("targetURL", target);

            session.removeAttribute(RegistrationConstants.DEADBODY_WITNESS_INFO_BEAN_ID);
        } finally {
            regDeadbodyFormBean = null;
        }
        return new ModelAndView("mostrecentdeadbodyregistrations", model);
    }

    /**
     * Add panchnama details of dead body, comes after search page Gets the dead
     * body registration screen taddpanchnama1.htm on load
     *
     * @param model ModelMap
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param session HttpSession
     * @return model
     * @throws Exception
     */
    @RequestMapping(value = "/taddpanchnama1.htm", method = RequestMethod.POST)
    public ModelAndView showAddPanchnama(final HttpServletRequest request,
            final ModelMap model, final HttpSession session) throws RegDeadbodyException {
        User user = (User) session.getAttribute(BeanNameResolver.getBeanName("USER_BEAN"));
        TDeadBodyRegistrationPanchnamaFormBean panchnamaFormBean = new TDeadBodyRegistrationPanchnamaFormBean();
        try {
            final Map<Integer, String> poCarryDbMap = deadbodybusinessDelegate.getPoliceOffiCarryDb(user);
            model.addAttribute("poCarryDbMap", poCarryDbMap);
            byte[] inquestNum = Base64.decodeBase64(request.getParameter("dbInquestNo"));
            byte[] inqDate = Base64.decodeBase64(request.getParameter("inqDate"));
            byte[] deadStatus = Base64.decodeBase64(request.getParameter("deadbodyStatus"));

            final String inqNoToDisp = (StrUtil.bytes2String(inquestNum).substring(StrUtil.bytes2String(inquestNum).length() - 4));
            request.setAttribute("inqNoToDisp", inqNoToDisp);
            panchnamaFormBean.setDbInquestNum(StrUtil.bytes2String(inquestNum));
            session.setAttribute("dbInquestNo", StrUtil.bytes2String(inquestNum));
            LOGGER.debug("Preparing to load panchnama form for inquest num :" + inquestNum);
            final String name = deadbodybusinessDelegate.getPanchnamaDeadPersonName(panchnamaFormBean, user);
            panchnamaFormBean.setDeadPersonFirstName(name);
            panchnamaFormBean.setInquestNum(Long.parseLong(StrUtil.bytes2String(inquestNum)));

            String langCode = session.getAttribute("langCD").toString();
            panchnamaFormBean.setLangCd(Integer.parseInt(langCode));
            panchnamaFormBean.setOriginalRecord(1);
            panchnamaFormBean.setStateCd(user.getStateCd().toString());
            panchnamaFormBean.setDistrictCd(user.getDistrictCd().toString());
            panchnamaFormBean.setPsCd(user.getPoliceStationCd().toString());
            panchnamaFormBean.setPerStateCd(user.getStateCd().toString());
            panchnamaFormBean.setPerDistrictCd(user.getDistrictCd().toString());
            panchnamaFormBean.setPerPsCd(user.getPoliceStationCd().toString());

            final Map<Integer, String> fileTypes = documentBusinessDelegate.dropDownListFileTypes(user);
            String hiddenDeathDate = deadbodybusinessDelegate.selectDeathDate3(panchnamaFormBean, user);

            model.addAttribute("fileTypes", fileTypes);
            model.addAttribute("deadbodyStatus", StrUtil.bytes2String(deadStatus));
            model.addAttribute("hiddenDeathDate", hiddenDeathDate);
            model.addAttribute("searchDeadBody", panchnamaFormBean);
            model.addAttribute("hiddenInqDateValue", StrUtil.bytes2String(inqDate));
            model.addAttribute("taddpanchnama1", panchnamaFormBean);
        } finally {
            panchnamaFormBean = null;
        }
        return new ModelAndView("taddpanchnama1", model);
    }

    /**
     * Add panchnama details of dead body, on click of submit this method is
     * called.
     *
     * @param panchnamaFormBean
     * @param result BindingResult
     * @param model ModelMap
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param session HttpSession
     * @param user User
     * @return model
     * @throws Exception
     */
    @RequestMapping(value = "/taddpanchnama1Post.htm", method = RequestMethod.POST)
    protected ModelAndView onSubmit(@ModelAttribute("taddpanchnama1") TDeadBodyRegistrationPanchnamaFormBean panchnamaFormBean,
            BindingResult result, ModelMap model, final HttpServletRequest request,
            final HttpSession session) throws RegDeadbodyException {
        ModelAndView modelAndView = null;
        DelegateWrapper wrapper = new DelegateWrapper();
        ErrorList errorList = new ErrorList();
        try {
            User user = (User) session.getAttribute(BeanNameResolver.getBeanName("USER_BEAN"));
            ArrayList FileList = (ArrayList) request.getAttribute("FileList");
            FileItem objFile = null;
            final String inquestNum = request.getParameter("dbInquestNo");
            session.setAttribute("dbInquestNo", inquestNum);
            if (!FileList.isEmpty()) {
                panchnamaFormBean.setFileList(FileList);
            }

            ArrayList witnessDetails = (ArrayList) session.getAttribute(RegistrationConstants.DEADBODY_WITNESS_INFO_BEAN_ID);
            wrapper.addListFormBean(RegistrationConstants.DEADBODY_WITNESS_INFO_BEAN_ID, witnessDetails);
            LOGGER.debug("Preparing to validate Panchanam form for inquest num:" + inquestNum);
            deadBodyAddPanchnamaValidator.validate(panchnamaFormBean, result);
            if (result.hasErrors()) {
                final String name = deadbodybusinessDelegate.getPanchnamaDeadPersonName(panchnamaFormBean, user);
                final Map<Integer, String> poCarryDbMap = deadbodybusinessDelegate.getPoliceOffiCarryDb(user);
                model.addAttribute("poCarryDbMap", poCarryDbMap);
                panchnamaFormBean.setDeadPersonFirstName(name);

                String langCode = session.getAttribute("langCD").toString();
                panchnamaFormBean.setLangCd(Integer.parseInt(langCode));
                panchnamaFormBean.setOriginalRecord(1);
                panchnamaFormBean.setStateCd(user.getStateCd().toString());
                panchnamaFormBean.setDistrictCd(user.getDistrictCd().toString());
                panchnamaFormBean.setPsCd(user.getPoliceStationCd().toString());
                panchnamaFormBean.setPerStateCd(user.getStateCd().toString());
                panchnamaFormBean.setPerDistrictCd(user.getDistrictCd().toString());
                panchnamaFormBean.setPerPsCd(user.getPoliceStationCd().toString());
                final Map<Integer, String> fileTypes = documentBusinessDelegate.dropDownListFileTypes(user);

                model.addAttribute("fileTypes", fileTypes);
                model.addAttribute("searchDeadBody", panchnamaFormBean);
                model.addAttribute("witnessList", witnessDetails);
                model.addAttribute("taddpanchnama1", panchnamaFormBean);
                modelAndView = new ModelAndView("taddpanchnama1", model);
                return modelAndView;
            }
            panchnamaFormBean.setLangCd(user.getLangCd());
            DeadBodyFilesFormBean file = new DeadBodyFilesFormBean();
            String errorMsg = "";
            final String inquestNo = String.valueOf(panchnamaFormBean.getDbInquestNum());
            final String shortInqNo = inquestNo.substring(inquestNo.length() - 4);
            ArrayList fileList = panchnamaFormBean.getFileList();
            session.setAttribute(CommonConstants.FORM_PRINT_KEY, panchnamaFormBean.getDbInquestNum());
            LOGGER.debug("FormBean data :" + panchnamaFormBean);
            LOGGER.debug("Validation error :" + result.getAllErrors());
            try {
                if (fileList != null) {
                    final Integer fileLength = fileList.size();
                    for (int i = 0; i < fileLength; i++) {
                        objFile = (FileItem) fileList.get(i);
                        file.setFileName(objFile.getName());
                        file.setFileType(objFile.getContentType());
                        file.setFile(objFile.get());
                        final long filesize = objFile.getSize();

                        if (filesize > 200000) {
                            panchnamaFormBean.setFileList(null);
                            uploadValidator.validate(panchnamaFormBean, result);
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.info("Exception during uploading file in Panchanma for inquest num:" + inquestNum);
                throw new RegDeadbodyException("Exception during uploading file in Panchanma", "MSG_507", e);
            } finally {
                objFile = null;
                file = null;
            }
            LOGGER.debug("Preparing to submit Panchanam details for inquest num:" + inquestNum);
            deadbodybusinessDelegate.panchnama(panchnamaFormBean, errorList, user, wrapper, witnessDetails);

            if (errorList.hasErrors()) {
                for (String errorCode : errorList.getErrors()) {
                    errorMsg = ResourceUtil.getCommonMessage(errorCode, user.getLangCd());
                    request.setAttribute("errorMsg", errorMsg);
                }
                return new ModelAndView(new RedirectView("mostrecentdeadbodyregistrations.htm?target=taddpanchnama1&save=failure&message=" + errorMsg + "&dbInquestNo=" + panchnamaFormBean.getDbInquestNum() + "&inqDate=" + request.getParameter("hiddenInqDateValue")));
            } else {
                request.setAttribute("dbListTopFive", ResourceUtil.getCommonMessage("MSG_813", user.getLangCd()));
                session.setAttribute(CommonConstants.MESSAGE_DEAD, shortInqNo);
                session.removeAttribute(RegistrationConstants.DEADBODY_WITNESS_INFO_BEAN_ID);
                return new ModelAndView(new RedirectView("mostrecentdeadbodyregistrations.htm?target=taddpanchnama1&save=success"));
            }
        } finally {
            wrapper = null;
            errorList = null;
        }
    }

    /**
     * Approval to waive post-mortem of dead body, comes after search page Gets
     * the dead body registration screen regwavepostmordeadbody1.htm on load
     *
     * @param model ModelMap
     * @param request HttpServletRequest
     * @param session HttpSession
     * @return model
     */
    @RequestMapping(value = "/regwavepostmordeadbody1.htm", method = RequestMethod.GET)
    public ModelAndView showWaivePostmortem(final ModelMap model, final HttpServletRequest request, final HttpSession session) {
        final User user = (User) session.getAttribute(BeanNameResolver.getBeanName("USER_BEAN"));
        TDeadBodyRegistrationApprovePostmorFormBean appPostmorFormBean = new TDeadBodyRegistrationApprovePostmorFormBean();
        TDeadBodyRegistrationFormBean dbSearchFormBean = new TDeadBodyRegistrationFormBean();
        try {
            byte[] inquestNum = Base64.decodeBase64(request.getParameter("dbInquestNo"));
            byte[] deadbodyStatus = Base64.decodeBase64(request.getParameter("deadbodyStatus"));
            String langCode = session.getAttribute("langCD").toString();
            final List deadbodyFileDetails = deadbodybusinessDelegate.getFileDetails(StrUtil.bytes2String(inquestNum), user.getLangCd());

            if (StrUtil.bytes2String(deadbodyStatus).equalsIgnoreCase("2")) {
                String message = null;
                final Map<String, Object> modelMap = new HashMap<String, Object>(2);
                message = ResourceUtil.getCommonMessage("MSG_82411", user.getLangCd());
                modelMap.put("rows", message);
                return new ModelAndView("jsonView", modelMap);
            } else {
                final ModelAndView modelandview = new ModelAndView("regwavepostmordeadbody1", model);
                appPostmorFormBean.setInquestNum(Long.parseLong(StrUtil.bytes2String(inquestNum)));
                appPostmorFormBean.setLangCd(Integer.parseInt(langCode));
                appPostmorFormBean.setOriginalRecord(1);
                request.setAttribute("inquestNum", StrUtil.bytes2String(inquestNum));
                model.addAttribute("regwavepostmordeadbody1", appPostmorFormBean);
                dbSearchFormBean = getSearchViewSubPage(dbSearchFormBean, StrUtil.bytes2String(inquestNum), request, model, session, true);
                model.addAttribute("nationalIdTypeListInformer", dbSearchFormBean.getNationalidtypeInfomer());
                model.addAttribute("nationalIdTypeListDeceased", dbSearchFormBean.getNationalidtypeDeceased());
                model.addAttribute("searchDeadBody", dbSearchFormBean);
                model.addAttribute("deadbodyFileDetails", deadbodyFileDetails);
                return modelandview;
            }
        } finally {
            appPostmorFormBean = null;
            dbSearchFormBean = null;
        }
    }

    /**
     * Approval to waive post-mortem of dead body, on click of submit this
     * method is called
     *
     * @param appPostmorFormBean TDeadBodyRegistrationApprovePostmorFormBean
     * @param request HttpServletRequest
     * @param session HttpSession
     * @param model ModelMap
     * @param searchDeadBody TDeadBodyRegistrationFormBean
     * @return model
     * @throws Exception
     */
    @RequestMapping(value = "/regwavepostmordeadbody1.htm", method = RequestMethod.POST)
    protected ModelAndView onSubmit(@ModelAttribute("regwavepostmordeadbody1") final TDeadBodyRegistrationApprovePostmorFormBean appPostmorFormBean,
            BindingResult result, ModelMap model, final HttpServletRequest request, final HttpSession session) throws RegDeadbodyException {
        DelegateWrapper wrapper = new DelegateWrapper();
        TDeadBodyRegistrationFormBean dbSearchFormBean = new TDeadBodyRegistrationFormBean();
        try {
            deadBodyRegWaiveValidator.validate(appPostmorFormBean, result);
            if (result.hasErrors()) {
                final User user = (User) session.getAttribute(BeanNameResolver.getBeanName("USER_BEAN"));
                model.addAttribute("regwavepostmordeadbody1", appPostmorFormBean);
                byte[] inquestNum = Base64.decodeBase64(request.getParameter("dbInquestNo"));
                byte[] deadbodyStatus = Base64.decodeBase64(request.getParameter("deadbodyStatus"));
                String langCode = session.getAttribute("langCD").toString();
                final List deadbodyFileDetails = deadbodybusinessDelegate.getFileDetails(StrUtil.bytes2String(inquestNum), user.getLangCd());

                if (StrUtil.bytes2String(deadbodyStatus).equalsIgnoreCase("2")) {
                    String message = null;
                    final Map<String, Object> modelMap = new HashMap<String, Object>(2);
                    message = ResourceUtil.getCommonMessage("MSG_82411", user.getLangCd());
                    modelMap.put("rows", message);
                    return new ModelAndView("jsonView", modelMap);
                } else {
                    appPostmorFormBean.setInquestNum(Long.parseLong(StrUtil.bytes2String(inquestNum)));
                    appPostmorFormBean.setLangCd(Integer.parseInt(langCode));
                    appPostmorFormBean.setOriginalRecord(1);
                    request.setAttribute("inquestNum", StrUtil.bytes2String(inquestNum));
                    model.addAttribute("regwavepostmordeadbody1", appPostmorFormBean);
                    dbSearchFormBean = getSearchViewSubPage(dbSearchFormBean, StrUtil.bytes2String(inquestNum), request, model, session, false);
                    dbSearchFormBean.setRegState(user.getStateWithLang());
                    dbSearchFormBean.setRegDistrict(user.getDistrictWithLang());
                    dbSearchFormBean.setRegPs(user.getPsWithLang());
                    model.addAttribute("nationalIdTypeListInformer", dbSearchFormBean.getNationalidtypeInfomer());
                    model.addAttribute("nationalIdTypeListDeceased", dbSearchFormBean.getNationalidtypeDeceased());
                    model.addAttribute("searchDeadBody", dbSearchFormBean);
                    model.addAttribute("deadbodyFileDetails", deadbodyFileDetails);
                    return new ModelAndView("regwavepostmordeadbody1", model);
                }
            }

            User user = (User) session.getAttribute(BeanNameResolver.getBeanName("USER_BEAN"));
            appPostmorFormBean.setLangCd(user.getLangCd());

            final String inquestNum = appPostmorFormBean.getInquestNum().toString();
            String errorMsg = "";
            final String inquestNo = String.valueOf(appPostmorFormBean.getInquestNum());
            final String shortInqNo = inquestNo.substring(inquestNo.length() - 4);

            session.setAttribute("inquestNum", inquestNum);
            deadbodybusinessDelegate.approvePostmortem(wrapper, appPostmorFormBean, user);
            if (wrapper.getErrorList().hasErrors()) {
                for (String errorCode : wrapper.getErrorList().getErrors()) {
                    errorMsg = ResourceUtil.getCommonMessage(errorCode, user.getLangCd());
                    request.setAttribute("errorMsg", errorMsg);
                }
                return new ModelAndView(new RedirectView("mostrecentdeadbodyregistrations.htm?target=regwavepostmordeadbody1&save=failure&message=" + errorMsg + "&dbInquestNo=" + inquestNum + "&inqDate=" + request.getParameter("hiddenInqDateValue")));
            } else {
                final String strMsg = ResourceUtil.getCommonMessage("MSG_815", user.getLangCd());
                request.setAttribute("dbListTopFive", strMsg);

                LOGGER.info("appPostmorFormBean.getPmWaiverAction()" + appPostmorFormBean.getPmWaiverAction());

                if (("Y").equals(appPostmorFormBean.getPmWaiverAction())) {
                    final String msgAction = ResourceUtil.getCommonMessage("APPROVED_MESSAGE", user.getLangCd());
                    session.setAttribute("msgAction", msgAction);
                } else {
                    final String msgAction = ResourceUtil.getCommonMessage("REJECTED_MESSAGE", user.getLangCd());
                    session.setAttribute("msgAction", msgAction);
                }

                session.setAttribute(CommonConstants.MESSAGE_DEAD, shortInqNo);
                return new ModelAndView(new RedirectView("mostrecentdeadbodyregistrations.htm?target=regwavepostmordeadbody1&save=success"));
            }
        } finally {
            wrapper = null;
            dbSearchFormBean = null;
        }
    }

    /**
     * Preparing post-mortem report of dead body, comes after search page Gets
     * the dead body registration screen postmortreq.htm on load
     *
     * @param request HttpServletRequest
     * @param session HttpSession
     * @param model ModelMap
     * @return model
     */
    @RequestMapping(value = "/postmortreq.htm", method = RequestMethod.POST)
    public ModelAndView showPreparePostmortem(final ModelMap model, final HttpServletRequest request, final HttpSession session) throws RegDeadbodyException {

        final User user = (User) session.getAttribute(BeanNameResolver.getBeanName("USER_BEAN"));
        PreparePostMortReqFormBean reqFormBean = new PreparePostMortReqFormBean();
        String isSentForPm = request.getParameter("isSentForPm");
        String isPostMortemAdd = request.getParameter("isPostMortemAdd");
        String postMortemTp = request.getParameter("postMortemTp");
        byte[] inquestNum = null;
        byte[] deadStatus = null;
        byte[] inqDate = null;
        try {


            if (request.getParameter("pendingTaskCallPreparePMR") != null) {
                LOGGER.info("Call for preparing post mortem request from EO/SHO/IO Dashbord Pending Tasks");
                inquestNum = Base64.decodeBase64(request.getParameter("dbInquestNoPreparePMR"));
                deadStatus = Base64.decodeBase64(request.getParameter("deadbodyStatusPreparePMR"));
                inqDate = Base64.decodeBase64(request.getParameter("inqDatePreparePMR"));
            } else {
                LOGGER.info("Call for preparing post mortem request from usecase");
                inquestNum = Base64.decodeBase64(request.getParameter("dbInquestNo"));
                session.setAttribute("inquestNumPm", inquestNum);
                deadStatus = Base64.decodeBase64(request.getParameter("deadbodyStatus"));
                inqDate = Base64.decodeBase64(request.getParameter("inqDate"));
            }

            final String inqNoToDisp = (StrUtil.bytes2String(inquestNum).substring(StrUtil.bytes2String(inquestNum).length() - 4));
            reqFormBean.setInquestNum(StrUtil.bytes2String(inquestNum));
            model.addAttribute("deadbodyStatus", StrUtil.bytes2String(deadStatus));
            model.addAttribute("hiddenInqDateValue", StrUtil.bytes2String(inqDate));
            request.setAttribute("inquestNum", reqFormBean.getInquestNum());
            request.setAttribute("inqNoToDisp", inqNoToDisp);
            model.addAttribute("isSentForPm", isSentForPm);
            model.addAttribute("isPostMortemAdd", isPostMortemAdd);
            model.addAttribute("postMortemTp", postMortemTp);
            try {
                reqFormBean.setOriginalRecord(1);
                reqFormBean = deadbodybusinessDelegate.selectDeadPersonInfo(reqFormBean, user);
                reqFormBean = deadbodybusinessDelegate.getInquestPmDetails(reqFormBean, user);
                String hiddenDeathDate = deadbodybusinessDelegate.selectDeathDate(reqFormBean, user);

                model.addAttribute("postmortreq", reqFormBean);
                model.addAttribute("hiddenDeathDate", hiddenDeathDate);
                model.addAttribute("districtCd", user.getDistrictCd().toString());
                reqFormBean.setDistrictCd(user.getDistrictCd().toString());


                final Map<Integer, String> poCarryDbMap = deadbodybusinessDelegate.getPoliceOffiCarryDb(user);
                model.addAttribute("poCarryDbMap", poCarryDbMap);
                final Map<Integer, String> districtMap = deadbodybusinessDelegate.getDistrictDetails(user.getStateCd(), user);
                model.addAttribute("districtMap", districtMap);
                final List historyList = deadbodybusinessDelegate.getPostMortemHistory(reqFormBean, user);
                model.addAttribute("historyList", historyList);
                LOGGER.info("The history is" + historyList);
                String dbInquestNum = reqFormBean.getInquestNum();
                final Long rowCount = deadbodybusinessDelegate.getRowCount(dbInquestNum, user);
                model.addAttribute("rowCount", rowCount);

            } catch (Exception e) {
                LOGGER.error("Error in getting prepare post-mortem view", e);
            }
        } finally {
            reqFormBean = null;
        }
        return new ModelAndView("postmortreq", model);
    }

    /**
     * Preparing post-mortem report of dead body, on click of submit this method
     * is called.
     *
     * @param postMortemRequestFormBean PreparePostMortReqFormBean
     * @param request HttpServletRequest
     * @param session HttpSession
     * @param model ModelMap
     * @return model
     * @throws Exception
     */
    @RequestMapping(value = "/postmortreqpost.htm", method = RequestMethod.POST)
    protected ModelAndView onSubmit(@ModelAttribute("postmortreq") PreparePostMortReqFormBean postMortemRequestFormBean,
            BindingResult result, ModelMap model, final HttpSession session, final HttpServletRequest request) throws RegDeadbodyException, NoSuchAlgorithmException {

        ModelAndView modelAndView = null;
        final User user = (User) session.getAttribute(BeanNameResolver.getBeanName("USER_BEAN"));
        DelegateWrapper wrapper = new DelegateWrapper();
        ErrorList errorList = new ErrorList();
        PreparePostMortReqFormBean reqFormBean = postMortemRequestFormBean;

        try {
            reqFormBean.setDistrictCd(user.getDistrictCd().toString());
            deadBodyPreparePostValidator.validate(reqFormBean, result);
            String postMortemTp = request.getParameter("postMortemTp");
            if (result.hasErrors()) {

                model.addAttribute("postmortreq", reqFormBean);
                modelAndView = new ModelAndView("postmortreq", model);
                final String inqNoToDisp = reqFormBean.getInquestNum().substring(reqFormBean.getInquestNum().length() - 4);
                model.addAttribute("deadbodyStatus", reqFormBean.getHiddenDeadbodyStatus());
                model.addAttribute("hiddenInqDateValue", reqFormBean.getHiddenInqDateValue());
                request.setAttribute("inqNoToDisp", inqNoToDisp);

                try {
                    reqFormBean.setOriginalRecord(1);
                    reqFormBean = deadbodybusinessDelegate.selectDeadPersonInfo(reqFormBean, user);
                    reqFormBean = deadbodybusinessDelegate.getInquestPmDetails(reqFormBean, user);
                    String hiddenDeathDate = deadbodybusinessDelegate.selectDeathDate(reqFormBean, user);
                    model.addAttribute("postmortreq", reqFormBean);
                    model.addAttribute("hiddenDeathDate", hiddenDeathDate);
                    final Map<Integer, String> poCarryDbMap = deadbodybusinessDelegate.getPoliceOffiCarryDb(user);
                    model.addAttribute("poCarryDbMap", poCarryDbMap);
                    final Map<Integer, String> districtMap = deadbodybusinessDelegate.getDistrictDetails(user.getStateCd(), user);
                    model.addAttribute("districtMap", districtMap);
                } catch (Exception e) {
                    LOGGER.error("Error in getting prepare post-mortem view", e);
                }
                return modelAndView;
            }

            reqFormBean.setLangCd(user.getLangCd());

            final String inquestNum = reqFormBean.getInquestNum().toString();
            String errorMsg = "";
            final String inquestNo = String.valueOf(reqFormBean.getInquestNum());
            final String shortInqNo = inquestNo.substring(inquestNo.length() - 4);

            LOGGER.debug("FormBean data :" + reqFormBean);
            LOGGER.debug("posrmort item sent:" + reqFormBean.getPmItemsSent());
            wrapper.addFormBean(RegistrationConstants.PREPARE_POSTMORT_REQ_ID, reqFormBean);
            if ("".equals(reqFormBean.getDpiPresPsId())) {
                reqFormBean.setDpiPresPsId(null);
            }
            if ("".equals(reqFormBean.getDpiPermPsId())) {
                reqFormBean.setDpiPermPsId(null);
            }

//            ******* START -- MedLEAPER -- SUMIT SAHA -- MARCH 2025 ********
            if (reqFormBean.getIsDataLive().equals("Y")) {

                reqFormBean.setHospitalCd(0);
            }
//******* END -- MedLEAPER -- SUMIT SAHA -- MARCH 2025 ********

            deadbodybusinessDelegate.savePreparePostMortReq(wrapper, errorList, user);

            session.setAttribute("postMortemTp", postMortemTp);
            session.setAttribute("inquestNum", inquestNum);
            session.setAttribute(CommonConstants.FORM_PRINT_KEY, inquestNum);
            if (errorList.hasErrors()) {
                for (String errorCode : errorList.getErrors()) {
                    errorMsg = ResourceUtil.getCommonMessage(errorCode, user.getLangCd());
                    request.setAttribute("errorMsg", errorMsg);
                }
                return new ModelAndView(new RedirectView("mostrecentdeadbodyregistrations.htm?target=postmortreq&save=failure&message=" + errorMsg + "&dbInquestNo=" + inquestNum + "&inqDate=" + request.getParameter("hiddenInqDateValue")));
            } else {
// ******* START -- MedLEAPER -- SUMIT SAHA -- MARCH 2025 ********
if (reqFormBean.getIsDataLive().equals("Y")) {

    PMRMedLeaperVO postData = new PMRMedLeaperVO();
    postData = deadbodybusinessDelegate.getPostData(inquestNum, user);

    // new line added
    postData.setNameOfIO(user.getFullName());   // Officer name
    postData.setIoRank(user.getRank());         // SI / Inspector
    // end new line

    postData.setHospitalName(reqFormBean.getHospitalNameLive());
    postData.setHospitalDistrict(reqFormBean.getHospitalDistrict());

    postData.setHospitalCode(reqFormBean.getLivehospitalCd().toString());
    postData.setHospitalStateCode(reqFormBean.getLiveStateCd().toString());
    postData.setHospitalDistrictCode(reqFormBean.getLiveDistrictCd().toString());

    String jsonData = postData.toJSON();
    LOGGER.info("Print String JSON data" + jsonData);

    /* ============================================================
     * NEW ADDITION (DATE: 15/01/2026)
     * Purpose: Capture IRAD responseCode + response body properly
     *          Set a session popup flag based on SUCCESS/FAIL
     * ============================================================ */
    int responseCode = -1;
    String successBody = "";
    String errorBody = "";

    try {
        BouncyCastleJsseProvider provider = new BouncyCastleJsseProvider(new BouncyCastleProvider());
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2", provider);
        sslContext.init(null, null, new SecureRandom());

        // URL url = new URL("https://irad.tripura.gov.in/irad/cctnsunnaturaldeath");
        // URL url = new URL("http://164.100.137.234:8082/alluser/all/cctnsunnaturaldeath");
        URL url = new URL("https://localhost:8443/irad/cctnsunnaturaldeath");

        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

        connection.setSSLSocketFactory(sslContext.getSocketFactory());
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        connection.setDoInput(true);
        connection.setDoOutput(true);

        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(jsonData.getBytes("UTF-8"));
        outputStream.close();

        responseCode = connection.getResponseCode();
        LOGGER.info("Status Code -- " + responseCode);

        // ✅ treat 200/201 as success (same like MLC)
        if (responseCode == 200 || responseCode == 201) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuffer sb = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                sb.append(inputLine);
            }
            in.close();
            successBody = sb.toString();
            LOGGER.info("Success Response: -- " + successBody);
        } else {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            String inputLine;
            StringBuffer sbErr = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                sbErr.append(inputLine);
            }
            in.close();
            errorBody = sbErr.toString();
            LOGGER.info("Error Response: -- " + errorBody);
        }

        connection.disconnect();

    } catch (Exception e) {
        errorBody = "Exception: " + e.getMessage();
        LOGGER.error("IRAD CALL FAILED: " + e.getMessage(), e);
    }

    // Decide SUCCESS / FAIL
    boolean pmrSuccess = (responseCode == 200 || responseCode == 201);

    // ✅ Save response to DB (same style as MLC)
    StringBuffer responseToSave = new StringBuffer(
        pmrSuccess ? "Success"
                   : (errorBody != null && errorBody.length() > 0 ? errorBody : "Failed")
    );

    registrationMlcBusinessDelegate.saveResponce(Long.parseLong(inquestNum), user, jsonData, responseToSave, "PMR");

    // ✅ Set popup flags in session (will be shown after redirect)
    if (pmrSuccess) {
        session.setAttribute("pmrSyncPopupStatus", "SUCCESS");
        session.setAttribute("pmrSyncPopupMsg", " ICJS/IRAD Response: Success. PMR data successfully synced and saved.");
    } else {
        session.setAttribute("pmrSyncPopupStatus", "FAIL");
        session.setAttribute("pmrSyncPopupMsg", " ICJS/IRAD Response: Failed. PMR data did NOT sync successfully. Please check IRAD response log/table.");
    }
}
// ******* END -- MedLEAPER -- SUMIT SAHA -- MARCH 2025 ********


                final String strMsg = ResourceUtil.getCommonMessage("MSG_808", user.getLangCd());
                request.setAttribute("savepreparepostmortreq", strMsg);
                final String strMsgRe = ResourceUtil.getCommonMessage("MSG_REP_REQ", user.getLangCd());
                request.setAttribute("savepreparepostmortreq", strMsgRe);
                session.setAttribute(CommonConstants.MESSAGE_DEAD, shortInqNo);
                return new ModelAndView(new RedirectView("mostrecentdeadbodyregistrations.htm?target=postmortreq&save=success"));
            }
        } finally {
            wrapper = null;
            errorList = null;
        }
    }

    /**
     * Used to get status of change enquiry
     *
     * @param request HttpServletRequest
     * @param session HttpSession
     * @return
     */
    @RequestMapping(value = "/changeenquirygetstatus.htm", method = RequestMethod.POST)
    public ModelAndView showChangeEnquiryStatus(final HttpServletRequest request, final HttpSession session) //throws RegDeadbodyException
    {
        final User user = (User) session.getAttribute(BeanNameResolver.getBeanName("USER_BEAN"));
        String PanchnamaStatus = null;
        final String inquestNum = request.getParameter("dbInquestNo");
        PanchnamaStatus = deadbodybusinessDelegate.getChangeEnquiryPreparedStatus(inquestNum, user);
        final Map<String, Object> modelMap = new HashMap<String, Object>(2);
        if (PanchnamaStatus != null) {
            modelMap.put("rows", PanchnamaStatus);
        }
        return new ModelAndView("jsonView", modelMap);
    }

    /**
     * Adding post-mortem report of dead body, comes after search page Gets the
     * dead body registration screen addpmrep.htm on load
     *
     * @param model ModelMap
     * @param request HttpServletRequest
     * @param session HttpSession
     * @return model
     * @throws RegDeadbodyException
     */
    @RequestMapping(value = "/addpmrep.htm", method = RequestMethod.POST)
    public ModelAndView showAddPostmortem(final ModelMap model, final HttpServletRequest request, final HttpSession session) throws RegDeadbodyException {
        final User user = (User) session.getAttribute(BeanNameResolver.getBeanName("USER_BEAN"));
        AddPostMortReportFormBean reportFormBean = new AddPostMortReportFormBean();
        ErrorList errorList = new ErrorList();
        byte[] inquestNum = null;
        byte[] deadStatus = null;
        byte[] inqDate = null;
        try {
            if (request.getParameter("pendingTaskCallAddPMR") != null) {
                LOGGER.info("Call for adding post mortem report from EO/SHO/IO Dashbord Pending Tasks");
                inquestNum = Base64.decodeBase64(request.getParameter("dbInquestNoAddPMR"));
                deadStatus = Base64.decodeBase64(request.getParameter("deadbodyStatusAddPMR"));
                inqDate = Base64.decodeBase64(request.getParameter("inqDateAddPMR"));
            } else {
                LOGGER.info("Call for adding post mortem report from usecase");
                inquestNum = Base64.decodeBase64(request.getParameter("dbInquestNo"));
                deadStatus = Base64.decodeBase64(request.getParameter("deadbodyStatus"));
                inqDate = Base64.decodeBase64(request.getParameter("inqDate"));
            }
            final String inqNoToDisp = (StrUtil.bytes2String(inquestNum).substring(StrUtil.bytes2String(inquestNum).length() - 4));
            model.addAttribute("hiddenInqDateValue", StrUtil.bytes2String(inqDate));
            model.addAttribute("deadbodyStatus", StrUtil.bytes2String(deadStatus));
            reportFormBean.setInquestNum(StrUtil.bytes2String(inquestNum));
            request.setAttribute("inqNoToDisp", inqNoToDisp);
            String remarkRepost = request.getParameter("remarkRepostMortem");
            String postMortType = request.getParameter("postMortemTp");
            LOGGER.info("The post mortem type issss" + postMortType);
            reportFormBean.setRemarkRepost(remarkRepost);
            reportFormBean.setPostMortType(postMortType);
            model.addAttribute("remarkRepost", remarkRepost);
            model.addAttribute("postMortType", postMortType);
            reportFormBean.setDbSerialNum(1);
            if (errorList.hasErrors()) {
                for (String errorCode : errorList.getErrors()) {
                    final String errorMsg = MessageSelector.getMessage(errorCode);
                    request.setAttribute("errorMsg", errorMsg);
                }
            }
            reportFormBean = deadbodybusinessDelegate.selectDeadPersonInfoForAddPmReport(reportFormBean, user);
            reportFormBean = deadbodybusinessDelegate.getInquestPmDetails(reportFormBean, user);
            deadbodybusinessDelegate.getDisplayHospitalAddress(reportFormBean, user);
            reportFormBean.setRiPresStateCd(user.getStateCd());
            reportFormBean.setRiPresDistrictCd(user.getDistrictCd());
            reportFormBean.setRiPresPsId(user.getPoliceStationCd());
            reportFormBean.setRiPermStateCd(user.getStateCd());
            reportFormBean.setRiPermDistrictCd(user.getDistrictCd());
            reportFormBean.setRiPermPsId(user.getPoliceStationCd());
            final Map<Integer, String> fileTypes = documentBusinessDelegate.dropDownListFileTypes(user);
            String hiddenDeathDate = deadbodybusinessDelegate.selectDeathDate1(reportFormBean, user);
            String dbIdentified = deadbodybusinessDelegate.selectDbIdentified(reportFormBean, user);
            model.addAttribute("fileTypes", fileTypes);
            model.addAttribute("addpmrep", reportFormBean);
            model.addAttribute("hiddenDeathDate", hiddenDeathDate);
            model.addAttribute("dbIdentified", dbIdentified);

            final Map<Integer, String> designationMap = deadbodybusinessDelegate.getDesignationMap(user);
            model.addAttribute("designationMap", designationMap);
            final Map<Integer, String> deathCauseMap = deadbodybusinessDelegate.getDeathCause(user);
            model.addAttribute("deathCauseMap", deathCauseMap);
            final Map<Integer, String> relativeTypeMap = deadbodybusinessDelegate.getRelativeType(user);
            model.addAttribute("relativeTypeMap", relativeTypeMap);
            final Map<Integer, String> countriesMap = deadbodybusinessDelegate.getCountries(user);
            model.addAttribute("countriesMap", countriesMap);
            final Map<Integer, String> statesMap = deadbodybusinessDelegate.getStates(user);
            model.addAttribute("statesMap", statesMap);
            final Map<Integer, String> districtMap = deadbodybusinessDelegate.getDistrictDetails(user.getStateCd(), user);
            model.addAttribute("districtMap", districtMap);
            final Map<Integer, String> psMap = deadbodybusinessDelegate.getPsDetails(user.getDistrictCd(), user);
            model.addAttribute("psMap", psMap);
            final List pmHistoryList = deadbodybusinessDelegate.getAddPostMortemHistory(reportFormBean, user);
            model.addAttribute("pmHistoryList", pmHistoryList);
            String dbInquestNum = reportFormBean.getInquestNum();
            final Long rowCount = deadbodybusinessDelegate.getRowCount(dbInquestNum, user);
            model.addAttribute("rowCount", rowCount);
            return new ModelAndView("addpmrep", model);

        } finally {
            errorList = null;
            reportFormBean = null;
        }
    }

    /**
     * Adding post-mortem report of dead body, on click of submit this method is
     * called.
     *
     * @param postMortemReportFormBean AddPostMortReportFormBean
     * @param request HttpServletRequest
     * @param session HttpSession
     * @param model ModelMap
     * @param user User
     * @return model
     * @throws Exception
     */
    @RequestMapping(value = "/addpmrepPage.htm", method = RequestMethod.POST)
    protected ModelAndView onSubmit(@ModelAttribute("addpmrep") AddPostMortReportFormBean postMortemReportFormBean,
            BindingResult result, ModelMap model, final HttpServletRequest request, final HttpSession session) throws RegDeadbodyException {
        ArrayList FileList = (ArrayList) request.getAttribute("FileList");
        AddPostMortReportFormBean reportFormBean = postMortemReportFormBean;
        DelegateWrapper wrapper = new DelegateWrapper();
        ErrorList errorList = new ErrorList();
        try {
            if (!FileList.isEmpty()) {
                reportFormBean.setFileList(FileList);

            }
            ModelAndView modelAndView = null;
            deadBodyAddPostValidator.validate(reportFormBean, result);
            String postMortemTp = request.getParameter("postMortType");
            String postMortemType = reportFormBean.getPostMortemRadioBtn();
            LOGGER.info("The post mortem 2nddddddd type issss" + postMortemTp);
            if (result.hasErrors()) {

                final ArrayList idTypeList = (ArrayList) session.getAttribute(RegistrationConstants.REG_DEADBODY_DOCTOR);
                wrapper.addListFormBean(RegistrationConstants.REG_DEADBODY_DOCTOR, idTypeList);
                final User user = (User) session.getAttribute(BeanNameResolver.getBeanName("USER_BEAN"));
                model.addAttribute("doctorList", idTypeList);
                model.addAttribute("addpmrep", reportFormBean);
                modelAndView = new ModelAndView("addpmrep", model);

                if (errorList.hasErrors()) {
                    for (String errorCode : errorList.getErrors()) {
                        final String errorMsg = MessageSelector.getMessage(errorCode);
                        request.setAttribute("errorMsg", errorMsg);
                    }
                }
                reportFormBean = deadbodybusinessDelegate.selectDeadPersonInfoForAddPmReport(reportFormBean, user);
                reportFormBean = deadbodybusinessDelegate.getInquestPmDetails(reportFormBean, user);
                deadbodybusinessDelegate.getDisplayHospitalAddress(reportFormBean, user);
                reportFormBean.setRiPresStateCd(user.getStateCd());
                reportFormBean.setRiPresDistrictCd(user.getDistrictCd());
                reportFormBean.setRiPresPsId(user.getPoliceStationCd());
                reportFormBean.setRiPermStateCd(user.getStateCd());
                reportFormBean.setRiPermDistrictCd(user.getDistrictCd());
                reportFormBean.setRiPermPsId(user.getPoliceStationCd());
                final Map<Integer, String> fileTypes = documentBusinessDelegate.dropDownListFileTypes(user);
                String hiddenDeathDate = deadbodybusinessDelegate.selectDeathDate1(reportFormBean, user);
                model.addAttribute("fileTypes", fileTypes);
                model.addAttribute("addpmrep", reportFormBean);
                model.addAttribute("hiddenDeathDate", hiddenDeathDate);

                final Map<Integer, String> designationMap = deadbodybusinessDelegate.getDesignationMap(user);
                model.addAttribute("designationMap", designationMap);
                final Map<Integer, String> deathCauseMap = deadbodybusinessDelegate.getDeathCause(user);
                model.addAttribute("deathCauseMap", deathCauseMap);
                final Map<Integer, String> relativeTypeMap = deadbodybusinessDelegate.getRelativeType(user);
                model.addAttribute("relativeTypeMap", relativeTypeMap);
                final Map<Integer, String> countriesMap = deadbodybusinessDelegate.getCountries(user);
                model.addAttribute("countriesMap", countriesMap);
                final Map<Integer, String> statesMap = deadbodybusinessDelegate.getStates(user);
                model.addAttribute("statesMap", statesMap);
                final Map<Integer, String> districtMap = deadbodybusinessDelegate.getDistrictDetails(user.getStateCd(), user);
                model.addAttribute("districtMap", districtMap);
                final Map<Integer, String> psMap = deadbodybusinessDelegate.getPsDetails(user.getDistrictCd(), user);
                model.addAttribute("psMap", psMap);
                return modelAndView;
            }
            final User user = (User) session.getAttribute(BeanNameResolver.getBeanName("USER_BEAN"));
            reportFormBean.setLangCd(user.getLangCd());

            final String inquestNum = reportFormBean.getInquestNum().toString();
            String errorMsg = "";
            final String inquestNo = String.valueOf(reportFormBean.getInquestNum());
            final String shortInqNo = inquestNo.substring(inquestNo.length() - 4);

            final ArrayList idTypeList = (ArrayList) session.getAttribute(RegistrationConstants.REG_DEADBODY_DOCTOR);
            wrapper.addListFormBean(RegistrationConstants.REG_DEADBODY_DOCTOR, idTypeList);
            reportFormBean.setDoctorList(idTypeList);
            reportFormBean.setHospitalCd(reportFormBean.getHospitalCd());

            wrapper.addFormBean(RegistrationConstants.ADD_POSTMORT_REPORT_ID, reportFormBean);
            if (reportFormBean.getRiPresPsId() == 0) {
                reportFormBean.setRiPresPsId(null);
            }
            if (reportFormBean.getRiPermPsId() == 0) {
                reportFormBean.setRiPermPsId(null);
            }
            if (("0").equals(reportFormBean.getRelativeType())) {
                reportFormBean.setRelativeType(null);
            }
            deadbodybusinessDelegate.saveAddPostMortReport(wrapper, errorList, user);
            session.setAttribute("postMortemTp", postMortemTp);
            session.setAttribute("inquestNum", inquestNum);
            session.setAttribute(CommonConstants.FORM_PRINT_KEY, inquestNum);
            if (errorList.hasErrors()) {
                for (String errorCode : errorList.getErrors()) {
                    errorMsg = ResourceUtil.getCommonMessage(errorCode, user.getLangCd());
                    request.setAttribute("errorMsg", errorMsg);
                }

                return new ModelAndView(new RedirectView("mostrecentdeadbodyregistrations.htm?target=addpmrep&save=failure&message=" + errorMsg + "&dbInquestNo=" + inquestNum + "&inqDate=" + request.getParameter("hiddenInqDateValue")));
            } else {
                final String strMsg = ResourceUtil.getCommonMessage("MSG_810", user.getLangCd());
                request.setAttribute("addpostmortreport", strMsg);
                final String strMsgRep = ResourceUtil.getCommonMessage("MSG_REP_REPORT", user.getLangCd());
                request.setAttribute("addpostmortreport", strMsgRep);
                session.setAttribute(CommonConstants.MESSAGE_DEAD, shortInqNo);
                session.removeAttribute(RegistrationConstants.REG_DEADBODY_DOCTOR);
                return new ModelAndView(new RedirectView("mostrecentdeadbodyregistrations.htm?target=addpmrep&save=success&postMortemTp=" + postMortemType));
            }
        } finally {
            wrapper = null;
            errorList = null;
        }
    }

    /**
     * Adding enquiry officer for dead body, comes after search page Gets the
     * dead body registration screen tdeadbodyregistration.htm on load
     *
     * @param request HttpServletRequest
     * @param session HttpSession
     * @param model ModelMap
     * @return model
     */
    @RequestMapping(value = "/tdeadbodyregistration.htm", method = RequestMethod.POST)
    public ModelAndView showAddEnquiryOfficer(final HttpServletRequest request,
            final ModelMap model, final HttpSession session) throws RegDeadbodyException {

        User user = (User) session.getAttribute(BeanNameResolver.getBeanName("USER_BEAN"));
        TDeadBodyRegistrationAddEnquiryFormBean addEnqFormBean = new TDeadBodyRegistrationAddEnquiryFormBean();
        TDeadBodyRegistrationFormBean dbSearchFormBean = new TDeadBodyRegistrationFormBean();
        byte[] inqDate = null;
        byte[] inquestNum = null;
        byte[] deadStatus = null;
//        ArrayList FileList = (ArrayList) request.getAttribute("FileList");
//        if (!FileList.isEmpty()) {
//            addEnqFormBean.setFileList(FileList);
//        }
        try {
            if (request.getParameter("pendingTaskCallAddER") != null) {
                inquestNum = Base64.decodeBase64(request.getParameter("dbInquestNoAddER"));
                deadStatus = Base64.decodeBase64(request.getParameter("deadbodyStatusAddER"));
                inqDate = Base64.decodeBase64(request.getParameter("inqDateAddER"));
            } else {
                inquestNum = Base64.decodeBase64(request.getParameter("dbInquestNo"));
                deadStatus = Base64.decodeBase64(request.getParameter("deadbodyStatus"));
                inqDate = Base64.decodeBase64(request.getParameter("inqDate"));
            }
            final String inqNoToDisp = (StrUtil.bytes2String(inquestNum).substring(StrUtil.bytes2String(inquestNum).length() - 4));
            final List deadbodyFileDetails = deadbodybusinessDelegate.getFileDetails(StrUtil.bytes2String(inquestNum), user.getLangCd());
            final List deadbodyPhotoDetails = deadbodybusinessDelegate.getPhotoDetails(StrUtil.bytes2String(inquestNum), user.getLangCd());
            String langCode = session.getAttribute("langCD").toString();
            if (langCode != null && !"0".equals(langCode) && !"".equals(langCode)) {
                user.setSearchlangcd(Integer.parseInt(langCode));
            }

            final Map<Integer, String> actionTakenMap = deadbodybusinessDelegate.getActionTaken(user);
            model.addAttribute("actionTakenMap", actionTakenMap);

            addEnqFormBean.setInquestNum(Long.parseLong(StrUtil.bytes2String(inquestNum)));

            model.addAttribute("deadbodyStatus", StrUtil.bytes2String(deadStatus));
            model.addAttribute("hiddenInqDateValue", StrUtil.bytes2String(inqDate));

            //addEnqFormBean = deadbodybusinessDelegate.getEnquiryDetForInquestNum(addEnqFormBean, user);
            addEnqFormBean.setLangCd(Integer.parseInt(langCode));
            addEnqFormBean.setOriginalRecord(1);
            request.setAttribute("inquestNum", StrUtil.bytes2String(inquestNum));

            String hiddenDeathDate = deadbodybusinessDelegate.selectDeathDate2(addEnqFormBean, user);
            dbSearchFormBean = getSearchViewSubPage(dbSearchFormBean, StrUtil.bytes2String(inquestNum), request, model, session, false);
            dbSearchFormBean.setRegState(user.getStateWithLang());
            dbSearchFormBean.setRegDistrict(user.getDistrictWithLang());
            dbSearchFormBean.setRegPs(user.getPsWithLang());
            populateSelectionFieldsData(model, session);
            List enquiryDetails = deadbodybusinessDelegate.getPreviousEnquiryDetails(addEnqFormBean, user);
            model.addAttribute("enquiryDetails", enquiryDetails);
            model.addAttribute("nationalIdTypeListInformer", dbSearchFormBean.getNationalidtypeInfomer());
            model.addAttribute("nationalIdTypeListDeceased", dbSearchFormBean.getNationalidtypeDeceased());
            model.addAttribute("searchDeadBody", dbSearchFormBean);
            model.addAttribute("tdeadbodyregistration", addEnqFormBean);
            model.addAttribute("deadbodyFileDetails", deadbodyFileDetails);
            model.addAttribute("deadbodyPhotoDetails", deadbodyPhotoDetails);
            model.addAttribute("hiddenDeathDate", hiddenDeathDate);
            request.setAttribute("inqNoToDisp", inqNoToDisp);
        } finally {
            addEnqFormBean = null;
            dbSearchFormBean = null;
        }
        return new ModelAndView("tdeadbodyregistration", model);
    }

    /**
     * Adding enquiry officer for dead body, on click of submit this method is
     * called
     *
     * @param addEnqFormBean
     * @param result BindingResult
     * @param model ModelMap
     * @param request HttpServletRequest
     * @param searchDeadBody TDeadBodyRegistrationFormBean
     * @return model
     * @throws Exception
     */
    @RequestMapping(value = "/tdeadbodyregistrationPost.htm", method = RequestMethod.POST)
    protected ModelAndView onSubmit(@ModelAttribute("tdeadbodyregistration") TDeadBodyRegistrationAddEnquiryFormBean addEnqFormBean,
            BindingResult result, ModelMap model, final HttpServletRequest request, final HttpSession session) throws RegDeadbodyException {
        TDeadBodyRegistrationFormBean dbSearchFormBean = new TDeadBodyRegistrationFormBean();
        ErrorList errorList = new ErrorList();
        try {
            deadBodyAddEnquiryValidator.validate(addEnqFormBean, result);
            if (result.hasErrors()) {
                model.addAttribute("tdeadbodyregistration", addEnqFormBean);

                User user = (User) session.getAttribute(BeanNameResolver.getBeanName("USER_BEAN"));

                byte[] inquestNum = Base64.decodeBase64(request.getParameter("dbInquestNo"));
                byte[] deadStatus = Base64.decodeBase64(request.getParameter("deadbodyStatus"));
                byte[] inqDate = Base64.decodeBase64(request.getParameter("inqDate"));
                final List deadbodyFileDetails = deadbodybusinessDelegate.getFileDetails(StrUtil.bytes2String(inquestNum), user.getLangCd());
                String langCode = session.getAttribute("langCD").toString();


                final Map<Integer, String> actionTakenMap = deadbodybusinessDelegate.getActionTaken(user);
                model.addAttribute("actionTakenMap", actionTakenMap);
                addEnqFormBean.setInquestNum(Long.parseLong(StrUtil.bytes2String(inquestNum)));
                model.addAttribute("deadbodyStatus", StrUtil.bytes2String(deadStatus));
                model.addAttribute("hiddenInqDateValue", StrUtil.bytes2String(inqDate));

                //addEnqFormBean = deadbodybusinessDelegate.getEnquiryDetForInquestNum(addEnqFormBean, user);
                addEnqFormBean.setLangCd(Integer.parseInt(langCode));
                addEnqFormBean.setOriginalRecord(1);
                request.setAttribute("inquestNum", StrUtil.bytes2String(inquestNum));
                String hiddenDeathDate = deadbodybusinessDelegate.selectDeathDate2(addEnqFormBean, user);
                dbSearchFormBean = getSearchViewSubPage(dbSearchFormBean, StrUtil.bytes2String(inquestNum), request, model, session, false);
                model.addAttribute("nationalIdTypeListInformer", dbSearchFormBean.getNationalidtypeInfomer());
                model.addAttribute("nationalIdTypeListDeceased", dbSearchFormBean.getNationalidtypeDeceased());
                model.addAttribute("searchDeadBody", dbSearchFormBean);
                model.addAttribute("tdeadbodyregistration", addEnqFormBean);
                model.addAttribute("deadbodyFileDetails", deadbodyFileDetails);
                model.addAttribute("hiddenDeathDate", hiddenDeathDate);
                return new ModelAndView("tdeadbodyregistration", model);

            }
            User user = (User) session.getAttribute(BeanNameResolver.getBeanName("USER_BEAN"));
            if (session != null) {
                user = (User) session.getAttribute(BeanNameResolver.getBeanName("USER_BEAN"));
            }
            if (user.getLangCd() == null) {
                if (session.getAttribute("langCD") != null) {
                    user.setLangCd((Integer) session.getAttribute("langCD"));
                }
            }
            if (session.getAttribute("dbEnquiryFileRemoveListSession") != null) {
                final ArrayList enquiryFileRemoveList = (ArrayList) session.getAttribute("dbEnquiryFileRemoveListSession");
                addEnqFormBean.setRemoveFileList(enquiryFileRemoveList);
            }
            final ArrayList FileList = (ArrayList) request.getAttribute("FileList");
            if (FileList != null) {
                addEnqFormBean.setFileList(FileList);
            }
            addEnqFormBean.setLangCd(user.getLangCd());
            String errorMsg = "";
            final String inquestNo = String.valueOf(addEnqFormBean.getInquestNum());
            final String shortInqNo = inquestNo.substring(inquestNo.length() - 4);

            LOGGER.debug("FormBean data :" + addEnqFormBean);
            deadbodybusinessDelegate.addEnqDetails(addEnqFormBean, errorList, user);
            if (errorList.hasErrors()) {
                for (String errorCode : errorList.getErrors()) {
                    errorMsg = ResourceUtil.getCommonMessage(errorCode, user.getLangCd());
                    request.setAttribute("errorMsg", errorMsg);
                }
                return new ModelAndView(new RedirectView("mostrecentdeadbodyregistrations.htm?target=tdeadbodyregistration&save=failure&message=" + errorMsg + "&dbInquestNo=" + addEnqFormBean.getInquestNum() + "&inqDate=" + request.getParameter("hiddenInqDateValue")));
            } else {
                final String strMsg = ResourceUtil.getCommonMessage("MSG_817", user.getLangCd());
                request.setAttribute("dbListTopFive", strMsg);

                session.setAttribute(CommonConstants.MESSAGE_DEAD, shortInqNo);
                return new ModelAndView(new RedirectView("mostrecentdeadbodyregistrations.htm?target=tdeadbodyregistration&save=success"));
            }
        } finally {
            dbSearchFormBean = null;
            errorList = null;
        }
    }

    /**
     * Changing enquiry officer for dead body, comes after search page Gets the
     * dead body registration screen tdeadbodyregistration.htm on load
     *
     * @param request HttpServletRequest
     * @param model ModelMap
     * @param session HttpSession
     * @return model
     */
    @RequestMapping(value = "/changeenquiryofficer1.htm", method = RequestMethod.POST)
    public ModelAndView showChangeEnquiry(final HttpServletRequest request,
            final ModelMap model, final HttpSession session) {

        User user = (User) session.getAttribute(BeanNameResolver.getBeanName("USER_BEAN"));
        TDeadBodyRegistrationChangeEnquiryFormBean changeEnqFormBean = new TDeadBodyRegistrationChangeEnquiryFormBean();
        TDeadBodyRegistrationFormBean dbSearchFormBean = new TDeadBodyRegistrationFormBean();
        try {
            byte[] inquestNum = null;
            byte[] deadStatus = null;
            if (request.getParameter("pendingTaskCallChangeEO") != null) {
                inquestNum = Base64.decodeBase64(request.getParameter("dbInquestNoChangeEO"));
                deadStatus = Base64.decodeBase64(request.getParameter("deadbodyStatusChangeEO"));
            } else {
                inquestNum = Base64.decodeBase64(request.getParameter("dbInquestNo"));
                deadStatus = Base64.decodeBase64(request.getParameter("deadbodyStatus"));
            }

            String langCode = session.getAttribute("langCD").toString();

            final List deadbodyFileDetails = deadbodybusinessDelegate.getFileDetails(StrUtil.bytes2String(inquestNum), user.getLangCd());

            changeEnqFormBean.setInquestNum(Long.parseLong(StrUtil.bytes2String(inquestNum)));
            changeEnqFormBean.setLangCd(Integer.parseInt(langCode));
            changeEnqFormBean.setOriginalRecord(1);
            String inquestNo = StrUtil.bytes2String(inquestNum);
            final Map<Integer, String> eoMap1 = deadbodybusinessDelegate.dropDownListForIO(user, inquestNo);
            model.addAttribute("eoList1", eoMap1);
            model.addAttribute("changeenquiryofficer1", changeEnqFormBean);
            dbSearchFormBean = getSearchViewSubPage(dbSearchFormBean, StrUtil.bytes2String(inquestNum), request, model, session, false);
            dbSearchFormBean.setRegState(user.getStateWithLang());
            dbSearchFormBean.setRegDistrict(user.getDistrictWithLang());
            dbSearchFormBean.setRegPs(user.getPsWithLang());
            model.addAttribute("nationalIdTypeListInformer", dbSearchFormBean.getNationalidtypeInfomer());
            model.addAttribute("nationalIdTypeListDeceased", dbSearchFormBean.getNationalidtypeDeceased());
            model.addAttribute("deadbodyFileDetails", deadbodyFileDetails);
            model.addAttribute("searchDeadBody", dbSearchFormBean);
            model.addAttribute("deadbodyStatus", StrUtil.bytes2String(deadStatus));
        } finally {
            changeEnqFormBean = null;
            dbSearchFormBean = null;
        }
        return new ModelAndView("changeenquiryofficer1", model);
    }

    /**
     * Changing enquiry officer for dead body, on click of submit this method is
     * called
     *
     * @param changeEnqFormBean TDeadBodyRegistrationChangeEnquiryFormBean
     * @param request HttpServletRequest
     * @param model ModelMap
     * @param searchDeadBody TDeadBodyRegistrationFormBean
     * @return model
     * @throws Exception
     */
    @RequestMapping(value = "/changeenquiryofficer1Post.htm", method = RequestMethod.POST)
    protected ModelAndView onSubmit(final @ModelAttribute("changeenquiryofficer1") TDeadBodyRegistrationChangeEnquiryFormBean changeEnqFormBean,
            BindingResult result, ModelMap model, final HttpServletRequest request, final HttpSession session) throws RegDeadbodyException {
        ModelAndView modelAndView = null;
        DelegateWrapper wrapper = new DelegateWrapper();
        TDeadBodyRegistrationFormBean dbSearchFormBean = new TDeadBodyRegistrationFormBean();
        try {
            deadBodyChangeEOValidator.validate(changeEnqFormBean, result);

            if (result.hasErrors()) {
                model.addAttribute("changeenquiryofficer1", changeEnqFormBean);
                modelAndView = new ModelAndView("changeenquiryofficer1", model);
                User user = (User) session.getAttribute(BeanNameResolver.getBeanName("USER_BEAN"));
                changeEnqFormBean.getInquestNum();
                String inqNo = changeEnqFormBean.getInquestNum().toString();
                String deadBodyStatus = request.getParameter("deadbStatus");
                //byte[] inquestNum = Base64.decodeBase64(request.getParameter("dbInquestNo"));
                String langCode = session.getAttribute("langCD").toString();
                //byte[] deadStatus = Base64.decodeBase64(request.getParameter("deadbodyStatus"));
                final List deadbodyFileDetails = deadbodybusinessDelegate.getFileDetails(inqNo, user.getLangCd());


                final Map<Integer, String> eoMap1 = deadbodybusinessDelegate.dropDownListForIO(user, inqNo);
                model.addAttribute("eoList1", eoMap1);
                changeEnqFormBean.setInquestNum(Long.parseLong(inqNo));
                changeEnqFormBean.setLangCd(Integer.parseInt(langCode));
                changeEnqFormBean.setOriginalRecord(1);
                model.addAttribute("changeenquiryofficer1", changeEnqFormBean);
                dbSearchFormBean = getSearchViewSubPage(dbSearchFormBean, inqNo, request, model, session, true);
                model.addAttribute("nationalIdTypeListInformer", dbSearchFormBean.getNationalidtypeInfomer());
                model.addAttribute("nationalIdTypeListDeceased", dbSearchFormBean.getNationalidtypeDeceased());
                model.addAttribute("deadbodyFileDetails", deadbodyFileDetails);
                model.addAttribute("searchDeadBody", dbSearchFormBean);
                model.addAttribute("deadbodyStatus", deadBodyStatus);

                return modelAndView;



            } else {
                User user = (User) session.getAttribute(BeanNameResolver.getBeanName("USER_BEAN"));
                if (session != null) {
                    user = (User) session.getAttribute(BeanNameResolver.getBeanName("USER_BEAN"));
                }
                if (user.getLangCd() == null) {
                    if (session.getAttribute("langCD") != null) {
                        user.setLangCd((Integer) session.getAttribute("langCD"));
                    }
                }
                changeEnqFormBean.setLangCd(user.getLangCd());
                String errorMsg = "";
                final ErrorList errorList = new ErrorList();
                final String inquestNo = String.valueOf(changeEnqFormBean.getInquestNum());
                final String shortInqNo = inquestNo.substring(inquestNo.length() - 4);

                deadbodybusinessDelegate.changeEnqOfficer(wrapper, changeEnqFormBean, user);
                if (wrapper.getErrorList().hasErrors()) {
                    for (String errorCode : wrapper.getErrorList().getErrors()) {
                        errorMsg = ResourceUtil.getCommonMessage(errorCode, user.getLangCd());
                        request.setAttribute("errorMsg", errorMsg);
                    }
                    modelAndView = new ModelAndView(new RedirectView("mostrecentdeadbodyregistrations.htm?target=changeenquiryofficer1&save=failure&message=" + errorMsg + "&dbInquestNo=" + changeEnqFormBean.getInquestNum() + "&inqDate=" + request.getParameter("hiddenInqDateValue")));
                } else {
                    final String strMsg = ResourceUtil.getCommonMessage("MSG_819", user.getLangCd());
                    request.setAttribute("dbListTopFive", strMsg);

                    session.setAttribute(CommonConstants.MESSAGE_DEAD, shortInqNo);
                    modelAndView = new ModelAndView(new RedirectView("mostrecentdeadbodyregistrations.htm?target=changeenquiryofficer1&save=success"));
                }
            }
        } finally {
            wrapper = null;
            dbSearchFormBean = null;
        }
        return modelAndView;
    }

    /**
     * Search screen
     *
     * @param searchFormBean TDeadBodyRegistrationFormBean
     * @param inquestNum String
     * @return searchFormBean
     */
    private TDeadBodyRegistrationFormBean getSearchViewSubPage(TDeadBodyRegistrationFormBean searchFormBean, final String inquestNum, final HttpServletRequest request, final ModelMap model, final HttpSession session, boolean status) {
        User user = (User) session.getAttribute(BeanNameResolver.getBeanName("USER_BEAN"));
        TDeadBodyRegistrationFormBean dbSearchFormBean = searchFormBean;
        DelegateWrapper wrapper = new DelegateWrapper();
        byte[] deadStatus = null;
        try {
            if (status) {
                if (request.getParameter("pendingTaskCallChangeEO") != null) {
                    deadStatus = Base64.decodeBase64(request.getParameter("deadbodyStatusChangeEO"));
                } else {
                    deadStatus = Base64.decodeBase64(request.getParameter("deadbodyStatus"));
                }

                wrapper.addFormBean(RegistrationConstants.REG_DEADBODY_VIEW_FORM_BEAN_ID, dbSearchFormBean);
                dbSearchFormBean = deadbodybusinessDelegate.deadbodyView(inquestNum, StrUtil.bytes2String(deadStatus), user);

            } else {
                //For Add Enquiry Report
                if (request.getParameter("pendingTaskCallAddER") != null) {
                    deadStatus = Base64.decodeBase64(request.getParameter("deadbodyStatusAddER"));

                }//For Change Enquiry Officer
                else if (request.getParameter("pendingTaskCallChangeEO") != null) {
                    deadStatus = Base64.decodeBase64(request.getParameter("deadbodyStatusChangeEO"));
                }//For Waive Post Mortem
                else {
                    deadStatus = Base64.decodeBase64(request.getParameter("deadbodyStatus"));
                }

                model.addAttribute("deadbodyStatus", StrUtil.bytes2String(deadStatus));
                wrapper.addFormBean(RegistrationConstants.REG_DEADBODY_VIEW_FORM_BEAN_ID, dbSearchFormBean);

                dbSearchFormBean = deadbodybusinessDelegate.deadbodyView(inquestNum, StrUtil.bytes2String(deadStatus), user);

            }
        } finally {
            wrapper = null;
        }
        return dbSearchFormBean;
    }

    /**
     * <p> Clear session values </p>
     *
     */
    private static void clearSessionAttributesDeadBody(HttpSession session) {
        if (session.getAttribute(RegistrationConstants.REG_DEADBODY_NATIONALITY) != null) {
            session.removeAttribute(RegistrationConstants.REG_DEADBODY_NATIONALITY);
        }
        if (session.getAttribute(RegistrationConstants.REG_DEADBODY_NATIONALITY1) != null) {
            session.removeAttribute(RegistrationConstants.REG_DEADBODY_NATIONALITY1);
        }
        if (session.getAttribute(RegistrationConstants.DEADBODY_WITNESS_INFO_BEAN_ID) != null) {
            session.removeAttribute(RegistrationConstants.DEADBODY_WITNESS_INFO_BEAN_ID);
        }
    }

    /**
     * Getting all the data from master tables and adding it to UI
     *
     * @param model ModelMap
     * @param session HttpSession
     */
    private void populateSelectionFieldsData(final ModelMap model, final HttpSession session) {
        User user = (User) session.getAttribute(BeanNameResolver.getBeanName("USER_BEAN"));
        final Map<Integer, String> fileTypes = documentBusinessDelegate.dropDownListFileTypes(user);
        model.addAttribute("fileTypes", fileTypes);
        final Map<Integer, String> photoFileType = proclaimedOffenderBusinessDelegate.dropDownListFileTypes(user);
        model.addAttribute("photoFileType", photoFileType);
        final Map<Integer, String> photoFileSubType = deadbodybusinessDelegate.getPhotoSubTypeList(CommonConstants.CONSTANT_ONE, user);
        model.addAttribute("photoFileSubType", photoFileSubType);
        final Map<Integer, String> countriesMap = deadbodybusinessDelegate.getCountries(user);
        model.addAttribute("countriesMap", countriesMap);
        final Map<Integer, String> relativeTypeMap = deadbodybusinessDelegate.getRelativeType(user);
        model.addAttribute("relativeTypeMap", relativeTypeMap);
        final Map<Integer, String> statesMap = deadbodybusinessDelegate.getStates(user);
        model.addAttribute("statesMap", statesMap);
        final Map<Integer, String> relationtypeMap = deadbodybusinessDelegate.getRelationtype(user);
        model.addAttribute("relationtypeMap", relationtypeMap);

        final Map<Integer, String> maritalstatusMap = deadbodybusinessDelegate.getDeceasedMaritalStatus(user);
        model.addAttribute("maritalstatusMap", maritalstatusMap);

        final Map<Integer, String> incomegroupMap = deadbodybusinessDelegate.getDeceasedIncomeGroup(user);
        model.addAttribute("incomegroupMap", incomegroupMap);
        final Map<Integer, String> educationMap = deadbodybusinessDelegate.getDeceasedEducationalQualification(user);
        model.addAttribute("educationMap", educationMap);

        final Map<Integer, String> casetypeMap = deadbodybusinessDelegate.getCasetype(user);
        model.addAttribute("casetypeMap", casetypeMap);
        Map<Integer, String> receiptMap = deadbodybusinessDelegate.getInformationMode(user);
        model.addAttribute("receiptList", receiptMap);
        final Map<Integer, String> idtypeMap = deadbodybusinessDelegate.getIdtype(user);
        model.addAttribute("idtypeMap", idtypeMap);
        final Map<Integer, String> religionMap = deadbodybusinessDelegate.getReligion(user);
        model.addAttribute("religionMap", religionMap);
        final Map<Integer, String> categoryMap = deadbodybusinessDelegate.getCategory(user);
        model.addAttribute("categoryMap", categoryMap);
        final Map<Integer, String> genderMap = deadbodybusinessDelegate.getGender(user);
        model.addAttribute("genderMap", genderMap);
        final Map<Integer, String> bodyBuildTypeMap = deadbodybusinessDelegate.dropDownListForBodyBuidTypes(user);
        model.addAttribute("bodyBuildTypeMap", bodyBuildTypeMap);
        final Map<Integer, String> bodyComplexionTypeMap = deadbodybusinessDelegate.dropDownListForBodyComplexionTypes(user);
        model.addAttribute("bodyComplexionTypeMap", bodyComplexionTypeMap);
//        final Map<Integer, String> dresscolorMap = deadbodybusinessDelegate.dropDownListForDressColor(user);
//        model.addAttribute("dresscolorMap", dresscolorMap);
        final Map<Integer, String> directionMap = deadbodybusinessDelegate.dropDownListForDirection(user);
        model.addAttribute("directionMap", directionMap);
        final Map<Integer, String> stainMap = deadbodybusinessDelegate.dropDownListForStain(user);
        model.addAttribute("stainMap", stainMap);
        final Map<Integer, String> districtMap = deadbodybusinessDelegate.getDistrictDetails(user.getStateCd(), user);
        model.addAttribute("districtMap", districtMap);
        final Map<Integer, String> psMap = deadbodybusinessDelegate.getPsDetails(user.getDistrictCd(), user);
        model.addAttribute("psMap", psMap);
        Map<Integer, String> blackMarksLargMap = iPhysicalFeaturesService.getBlackMarksLargeMap(user);
        model.addAttribute("blackMarksLargMap", blackMarksLargMap);
        Map<Integer, String> relationTypeMap = iPhysicalFeaturesService.dropDownListForRelation(user);
        model.addAttribute("relationTypeMapGeneral", relationTypeMap);
        final Map<Integer, String> eoMap1 = deadbodybusinessDelegate.dropDownListForIO1(user);
        model.addAttribute("eoList1", eoMap1);
        final Map<Integer, String> approverMap = deadbodybusinessDelegate.deadApproval(user);
        model.addAttribute("approverMap", approverMap);
        final Map<Integer, String> srcOfInfrmMap = deadbodybusinessDelegate.getSourceOfInformation(user);
        model.addAttribute("sourceOfInformation", srcOfInfrmMap);
        final Map<Integer, String> deceasedOccupation = deadbodybusinessDelegate.getDeceasedOccupation(user);
        model.addAttribute("deceasedOccupationList", deceasedOccupation);
        final Map<String, String> officersMap = businessDelegate.getOfficers(user, user.getLangCd());
        model.addAttribute("officersList", officersMap);
    }

    /**
     * To get Gd date based on gd number
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @return
     */
    @RequestMapping(value = "/getUAPGdDateDeadBody.htm", method = RequestMethod.POST)
    public ModelAndView gDDateFetch(HttpServletRequest request,
            HttpServletResponse response, HttpSession session) {

        User user = (User) session.getAttribute(BeanNameResolver.getBeanName("USER_BEAN"));
        Integer psCd = user.getPoliceStationCd();

        int gdNum = Integer.parseInt(request.getParameter("gdNum"));
        String gdDate = deadbodybusinessDelegate.fetchUAPGdDateDeadBody(gdNum, psCd, user);
        Map<String, Object> modelMap = new HashMap<String, Object>(2);

        modelMap.put("rows", gdDate);
        return new ModelAndView("jsonView", modelMap);

    }

    /**
     * To load witness pop up
     *
     * @param panchnamaFormBean TDeadBodyRegistrationPanchnamaFormBean
     * @param request HttpServletRequest
     * @param model ModelMap
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/witnessinfopopup.htm", method = RequestMethod.GET)
    public ModelAndView getWitnessInfo(@ModelAttribute("witnessinfopopup") TDeadBodyRegistrationPanchnamaFormBean panchnamaFormBean, HttpServletRequest request, ModelMap model, HttpSession session) throws ApplicationException {
        User user = (User) session.getAttribute(BeanNameResolver.getBeanName("USER_BEAN"));

        final Map<Integer, String> genderMap = deadbodybusinessDelegate.getGender(user);
        model.addAttribute("genderMap", genderMap);
        final Map<Integer, String> relativeMap = deadbodybusinessDelegate.getRelativeType(user);
        model.addAttribute("relativeMap", relativeMap);
        final Map<Integer, String> countriesMap = deadbodybusinessDelegate.getCountries(user);
        model.addAttribute("countriesMap", countriesMap);
        final Map<Integer, String> statesMap = deadbodybusinessDelegate.getStates(user);
        model.addAttribute("statesMap", statesMap);
        final Map<Integer, String> districtMap = deadbodybusinessDelegate.getDistrictDetails(user.getStateCd(), user);
        model.addAttribute("districtMap", districtMap);
        final Map<Integer, String> psMap = deadbodybusinessDelegate.getPsDetails(user.getDistrictCd(), user);
        model.addAttribute("psMap", psMap);
        final String inquestNum = (String) session.getAttribute("dbInquestNo");
        panchnamaFormBean.setDbInquestNum(inquestNum);
        panchnamaFormBean.setStateCd(user.getStateCd().toString());
        panchnamaFormBean.setDistrictCd(user.getDistrictCd().toString());
        panchnamaFormBean.setPsCd(user.getPoliceStationCd().toString());
        panchnamaFormBean.setPerStateCd(user.getStateCd().toString());
        panchnamaFormBean.setPerDistrictCd(user.getDistrictCd().toString());
        panchnamaFormBean.setPerPsCd(user.getPoliceStationCd().toString());
        return new ModelAndView("witnessinfopopup");
    }

    /**
     * Saving witness information
     *
     * @param panchnamaFormBean TDeadBodyRegistrationPanchnamaFormBean
     * @param result BindingResult
     * @param model ModelMap
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param user User
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/witnessinfopopup.htm", method = RequestMethod.POST)
    public ModelAndView addWitnessInfo(@ModelAttribute("witnessinfopopup") TDeadBodyRegistrationPanchnamaFormBean panchnamaFormBean, BindingResult result, ModelMap model, HttpServletRequest request, HttpServletResponse response, User user, HttpSession session) throws ApplicationException {

        List witnessDetails = (ArrayList) session.getAttribute(RegistrationConstants.DEADBODY_WITNESS_INFO_BEAN_ID);
        if (witnessDetails == null) {
            witnessDetails = new ArrayList();
        }
        ModelAndView modelAndView = null;
        if (session != null) {
            user = (User) session.getAttribute(BeanNameResolver.getBeanName("USER_BEAN"));
        }
        if (user.getLangCd() == null) {
            if (session.getAttribute("langCD") != null) {
                user.setLangCd((Integer) session.getAttribute("langCD"));
            }
        }
        final String inquestNum = (String) session.getAttribute("dbInquestNo");

        panchnamaFormBean.setDbInquestNum(inquestNum);
        panchnamaFormBean.setLangCd(user.getLangCd());
        if (result.hasErrors()) {

            modelAndView = new ModelAndView("witnessinfopopup", model);
        } else {

            witnessDetails.add(panchnamaFormBean);
            session.setAttribute(RegistrationConstants.DEADBODY_WITNESS_INFO_BEAN_ID, witnessDetails);

            String strMsg = "Witness Information Saved Successfully";
            String success = "success";
            request.setAttribute("errorMsgSeiz", null);

            modelAndView = new ModelAndView(new RedirectView("witnessinfopopup.htm?propertySeizSuccess=" + strMsg + "&displaySuccessMsg=" + success));

        }

        return modelAndView;

    }

    /**
     * To get status of whether FIR is registered
     *
     * @param request HttpServletRequest
     * @param session HttpSession
     * @return
     */
    @RequestMapping(value = "/addenquirygetstatus.htm", method = RequestMethod.POST)
    public ModelAndView showAddEnquiryStatus(final HttpServletRequest request, final HttpSession session) //throws RegDeadbodyException
    {
        final User user = (User) session.getAttribute(BeanNameResolver.getBeanName("USER_BEAN"));
        String PanchnamaStatus = null;
        final String inquestNum = request.getParameter("dbInquestNo");
        PanchnamaStatus = deadbodybusinessDelegate.getAddEnquiryPreparedStatus(inquestNum, user);
        final Map<String, Object> modelMap = new HashMap<String, Object>(2);
        if (PanchnamaStatus != null) {
            modelMap.put("rows", PanchnamaStatus);
        }
        return new ModelAndView("jsonView", modelMap);
    }

    /**
     * Method to populate the search result after registration
     *
     * @param modelMap Model
     * @param request Request Object
     * @return Model and View
     */
    @RequestMapping(value = "unidentifiedFoundMatchDeadBody.htm", method = RequestMethod.GET)
    public ModelAndView getUifpSearchMatch(@ModelAttribute("unidentifiedFoundMatchDeadBody") ModelMap modelMap, HttpServletRequest request) {
        return new ModelAndView("unidentifiedFoundMatchDeadBody", modelMap);
    }

    /**
     * method to match result found with Registered Un Identified Found Person
     *
     * @param foundPersonVO Form Bean
     * @param session Session Variable
     * @param model Model Attribute
     * @param request Request Object
     * @return Model and view
     * @throws Exception Throws Exception
     */
    @RequestMapping(value = "/unidentifiedFoundMatchDeadBody.htm", method = RequestMethod.POST)
    protected ModelAndView submitMatchFound(@ModelAttribute("regDeadbodyFormBean") TDeadBodyRegistrationFormBean regDeadbodyFormBean,
            HttpSession session, ModelMap model, HttpServletRequest request) throws ApplicationException {
        ModelAndView modelview = null;
        User user = null;
        if (session != null) {
            user = (User) session.getAttribute(BeanNameResolver.getBeanName("USER_BEAN"));
        }
        deadbodybusinessDelegate.saveMatchPersonSearch(regDeadbodyFormBean, user);

        modelview = new ModelAndView(new RedirectView("regdeadbody.htm"));
        final String inquestNo = String.valueOf(regDeadbodyFormBean.getInquestNum());
        final String regnoViewFormat = inquestNo.substring(inquestNo.length() - 4);
        session.setAttribute(CommonConstants.MESSAGE_DEAD, regnoViewFormat);
        session.setAttribute(CommonConstants.FOUNDPERSON_MATCH_FOUND, true);
        return modelview;
    }

    /**
     * Method used to display the previous enquiry details information pop up
     * for a Inquest Enquiry Serial No.
     *
     * @param addEnqFormBean
     * @param model
     * @param request
     * @param response
     * @return ModelAndView
     * @throws Exception
     */
    @RequestMapping(value = "previousenquirydetailspopup.htm", method = RequestMethod.GET)
    public ModelAndView getPreviousEnquiryDetails(@ModelAttribute("previousenquirydetailspopup") TDeadBodyRegistrationAddEnquiryFormBean addEnqFormBean, final ModelMap model, final HttpServletRequest request,
            final HttpServletResponse response, final HttpSession session) throws ApplicationException {

        User user = (User) session.getAttribute(BeanNameResolver.getBeanName("USER_BEAN"));
        LOGGER.debug("Fetching previous enquiry details for dead body inquest enquiry num - " + addEnqFormBean.getUidbEoSrno() + " by user : " + user.getPsStaffCd());
        addEnqFormBean = deadbodybusinessDelegate.getEnquiryDetForInquestNum(addEnqFormBean, user);
        model.addAttribute("previousenquirydetailspopup", addEnqFormBean);
        return new ModelAndView("previousenquirydetailspopup", model);
    }

    /**
     * Function to display image in matching result with Missing person and
     * UIFP/ photo of dead body
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param session HttpSession
     */
    @RequestMapping(value = "/retrieveImage.htm", method = RequestMethod.GET)
    public void retrieveImage(HttpServletRequest request,
            HttpServletResponse response, HttpSession session) {
        ByteArrayOutputStream baos = null;
        try {

            String regNum = request.getParameter("id");
            String type = request.getParameter("type");
            byte[] imageArray = deadbodybusinessDelegate.getImageByteArray(regNum, type);
            LOGGER.info("Calling function to get image with person type cd " + type + " and registration number " + regNum);
            response.setContentType("image/jpg");

            if (imageArray == null) {
                LOGGER.info("No Front view Photograph available ");
                BufferedImage originalImage = ImageIO.read(new File(servletContext.getRealPath("./img/faceoutline_1.gif")));
                baos = new ByteArrayOutputStream();
                ImageIO.write(originalImage, "gif", baos);
                baos.flush();
                imageArray = baos.toByteArray();
                baos.close();
                response.getOutputStream().write(imageArray, 0, imageArray.length);
                response.getOutputStream().close();
            } else {
                response.getOutputStream().write(imageArray, 0, imageArray.length);
                response.getOutputStream().close();
                LOGGER.info("Photograph present ");
            }
        } catch (IOException ex) {
            LOGGER.error("IO Exception while reading image in uidb"
                    + ex.getMessage());
        } finally {
            baos = null;
        }
    }

    /**
     * Function for printing InquestOfDeadBody
     *
     * @param request current HTTP request
     * @param response current HTTP response
     * @param session current HTTP session
     * @throws ClassNotFoundException if any error occurs
     * @throws IOException if any error occurs
     * @throws SQLException if any error occurs
     * @throws JRException if any error occurs
     */
    @RequestMapping(value = "/printInquestOfDeadBody.htm", method = RequestMethod.GET)
    public void printInquestOfDeadBody(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws ClassNotFoundException, IOException, SQLException, JRException {

        User user = (User) session.getAttribute(BeanNameResolver.getBeanName(USER_BEAN));
        try {
            String path = "";
            final HashMap map = new HashMap();
            final String inquestNumber = session.getAttribute(CommonConstants.FORM_PRINT_KEY).toString();

            final String pdfName = "Inquest Of DeadBody";
            final String uniqeid = inquestNumber;

            path = "/AdditionalPrintouts/InquestOfDeadBody//" + ReportsConstants.INQUEST_OF_DEAD_BODY;

            String subPath = null;
            subPath = request.getRealPath("/WEB-INF/reports/AdditionalPrintouts/InquestOfDeadBody/") + "/";
            map.put("SUBREPORT_DIR", subPath);
            map.put("Lang_cd", user.getLangCd().toString());
            map.put("InquestNumber", inquestNumber);

            jasperReportsUtil.getJasperPdfviewerFromImg(path, map, request, response, pdfName, uniqeid);
        } catch (Exception e) {

            LOGGER.error("**********************************Exception from printInquestOfDeadBody**************"
                    + e.getMessage());
        }
    }

    /**
     * Function for printing PostMortem Request Report
     *
     * @param request current HTTP request
     * @param response current HTTP response
     * @param session current HTTP session
     * @throws ClassNotFoundException if any error occurs
     * @throws IOException if any error occurs
     * @throws SQLException if any error occurs
     * @throws JRException if any error occurs
     */
    @RequestMapping(value = "/printPostmortemReport.htm", method = RequestMethod.GET)
    public void printPostmortemReport(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws ClassNotFoundException, IOException, SQLException, JRException {

        User user = (User) session.getAttribute(BeanNameResolver.getBeanName(USER_BEAN));
        try {
            String path = "";
            final HashMap map = new HashMap();
            final String inquestNumber = session.getAttribute(CommonConstants.FORM_PRINT_KEY).toString();

            final String pdfName = "PostMortemExaminationReport";
            final String uniqeid = inquestNumber;

            path = "/AdditionalPrintouts/PostMortemExaminationReport//" + ReportsConstants.POSTMORTEM_EXAM_REPORT;

            String subPath = null;
            subPath = request.getRealPath("/WEB-INF/reports/AdditionalPrintouts/PostMortemExaminationReport/") + "/";
            map.put("SUBREPORT_DIR", subPath);
            map.put("Lang_cd", user.getLangCd().toString());
            map.put("Inquest_num", inquestNumber);

            jasperReportsUtil.getJasperPdfviewerFromImg(path, map, request, response, pdfName, uniqeid);
        } catch (Exception e) {

            LOGGER.error("**********************************Exception from printInquestOfDeadBody**************"
                    + e.getMessage());
        }
    }

    /**
     * Function for printing PostMortem Request Report
     *
     * @param request current HTTP request
     * @param response current HTTP response
     * @param session current HTTP session
     * @throws ClassNotFoundException if any error occurs
     * @throws IOException if any error occurs
     * @throws SQLException if any error occurs
     * @throws JRException if any error occurs
     */
    @RequestMapping(value = "/preparePostmortemRequest.htm", method = RequestMethod.GET)
    public void preparePostmortemRequest(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws ClassNotFoundException, IOException, SQLException, JRException {

        User user = (User) session.getAttribute(BeanNameResolver.getBeanName(USER_BEAN));
        try {
            String path = "";
            final HashMap map = new HashMap();
            final String inquestNumber = session.getAttribute(CommonConstants.FORM_PRINT_KEY).toString();

            final String pdfName = "PostMortemRequestReport";
            final String uniqeid = inquestNumber;

            path = "/AdditionalPrintouts/postmortemRequestReport//" + ReportsConstants.POSTMORTEM_REQUEST_REPORT;

            String subPath = null;
            subPath = request.getRealPath("/WEB-INF/reports/AdditionalPrintouts/postmortemRequestReport/") + "/";
            map.put("SUBREPORT_DIR", subPath);
            map.put("Langcd", user.getLangCd().toString());
            map.put("InquestNo", inquestNumber);

            jasperReportsUtil.getJasperPdfviewerFromImg(path, map, request, response, pdfName, uniqeid);
        } catch (Exception e) {

            LOGGER.error("**********************************Exception from printPostmortemRequest**************"
                    + e.getMessage());
        }
    }

    /**
     * Function for printing RePostMortem Request Report
     *
     * @param request current HTTP request
     * @param response current HTTP response
     * @param session current HTTP session
     * @throws ClassNotFoundException if any error occurs
     * @throws IOException if any error occurs
     * @throws SQLException if any error occurs
     * @throws JRException if any error occurs
     */
    @RequestMapping(value = "/prepareRePostMortemRequest.htm", method = RequestMethod.GET)
    public void prepareRePostMortemRequest(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws ClassNotFoundException, IOException, SQLException, JRException {

        User user = (User) session.getAttribute(BeanNameResolver.getBeanName(USER_BEAN));
        try {
            String path = "";
            final HashMap map = new HashMap();
            final String inquestNumber = session.getAttribute(CommonConstants.FORM_PRINT_KEY).toString();

            final String pdfName = "RePostMortemRequestReport";
            final String uniqeid = inquestNumber;

            path = "/AdditionalPrintouts/RepostmortemRequestReport//" + ReportsConstants.REPOSTMORTEM_REQUEST_REPORT;

            String subPath = null;
            subPath = request.getRealPath("/WEB-INF/reports/AdditionalPrintouts/RepostmortemRequestReport/") + "/";
            map.put("SUBREPORT_DIR", subPath);
            map.put("Langcd", user.getLangCd().toString());
            map.put("InquestNo", inquestNumber);

            jasperReportsUtil.getJasperPdfviewerFromImg(path, map, request, response, pdfName, uniqeid);
        } catch (Exception e) {

            LOGGER.error("**********************************Exception from printRePostmortemRequest**************"
                    + e.getMessage());
        }
    }

    /**
     * Function for printing RePostMortem Request Report
     *
     * @param request current HTTP request
     * @param response current HTTP response
     * @param session current HTTP session
     * @throws ClassNotFoundException if any error occurs
     * @throws IOException if any error occurs
     * @throws SQLException if any error occurs
     * @throws JRException if any error occurs
     */
    @RequestMapping(value = "/printRePostMortemReport.htm", method = RequestMethod.GET)
    public void printRePostMortemReport(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws ClassNotFoundException, IOException, SQLException, JRException {

        User user = (User) session.getAttribute(BeanNameResolver.getBeanName(USER_BEAN));
        try {
            String path = "";
            final HashMap map = new HashMap();
            final String inquestNumber = session.getAttribute(CommonConstants.FORM_PRINT_KEY).toString();

            final String pdfName = "RePostMortemExamReport";
            final String uniqeid = inquestNumber;

            path = "/AdditionalPrintouts/RePostMortemExaminationReport//" + ReportsConstants.REPOSTMORTEM_EXAM_REPORT;

            String subPath = null;
            subPath = request.getRealPath("/WEB-INF/reports/AdditionalPrintouts/RePostMortemExaminationReport/") + "/";
            map.put("SUBREPORT_DIR", subPath);
            map.put("Lang_cd", user.getLangCd().toString());
            map.put("Inquest_num", inquestNumber);

            jasperReportsUtil.getJasperPdfviewerFromImg(path, map, request, response, pdfName, uniqeid);
        } catch (Exception e) {

            LOGGER.error("**********************************Exception from printRepostMortemReport**************"
                    + e.getMessage());
        }
    }

    @RequestMapping(value = "/prepareRepDeadBodyDescription.htm", method = RequestMethod.GET)
    public void prepareRepDeadBodyDescreption(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws ClassNotFoundException, IOException, SQLException, JRException {

        User user = (User) session.getAttribute(BeanNameResolver.getBeanName(USER_BEAN));
        try {
            String path = "";
            final HashMap map = new HashMap();
            final String inquestNum = session.getAttribute(CommonConstants.FORM_PRINT_KEY).toString();

            final String pdfName = "RePostMortemRequestReportDescription";
            final String uniqeid = inquestNum;

            path = "/AdditionalPrintouts/RepostmortemReportDescription//" + ReportsConstants.REPOSTMORTEM_REQUEST_DESCRIPTION;

            String subPath = null;
            subPath = request.getRealPath("/WEB-INF/reports/AdditionalPrintouts/RepostmortemReportDescription/") + "/";
            map.put("SUBREPORT_DIR", subPath);
            map.put("Langcd", user.getLangCd().toString());
            map.put("InquestNo", inquestNum);

            jasperReportsUtil.getJasperPdfviewerFromImg(path, map, request, response, pdfName, uniqeid);
        } catch (Exception e) {

            LOGGER.error("**********************************Exception from prepareRepDeadBodyDescreption**************"
                    + e.getMessage());
        }
    }

    @RequestMapping(value = "/prepareDeadBodyDescreption.htm", method = RequestMethod.GET)
    public void prepareDeadBodyDescreption(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws ClassNotFoundException, IOException, SQLException, JRException {

        User user = (User) session.getAttribute(BeanNameResolver.getBeanName(USER_BEAN));
        try {
            String path = "";
            final HashMap map = new HashMap();
            final String inquestNum = session.getAttribute(CommonConstants.FORM_PRINT_KEY).toString();

            final String pdfName = "PostMortemRequestReportDescription";
            final String uniqeid = inquestNum;

            path = "/AdditionalPrintouts/postmortemReportDescription//" + ReportsConstants.POSTMORTEM_REQUEST_DESCREPTION;

            String subPath = null;
            subPath = request.getRealPath("/WEB-INF/reports/AdditionalPrintouts/postmortemReportDescription/") + "/";
            map.put("SUBREPORT_DIR", subPath);
            map.put("Langcd", user.getLangCd().toString());
            map.put("InquestNo", inquestNum);

            jasperReportsUtil.getJasperPdfviewerFromImg(path, map, request, response, pdfName, uniqeid);
        } catch (Exception e) {

            LOGGER.error("**********************************Exception from printDeadBodyDescreption**************"
                    + e.getMessage());
        }
    }

    /**
     * Function for print UnIdentified DeadBody
     *
     * @param request current HTTP request
     * @param response current HTTP response
     * @param session current HTTP session
     * @throws ClassNotFoundException if any error occurs
     * @throws IOException if any error occurs
     * @throws SQLException if any error occurs
     * @throws JRException if any error occurs
     */
    @RequestMapping(value = "/printUnIdentifiedDeadBody.htm", method = RequestMethod.GET)
    public void printUnIdentifiedDeadBody(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws ClassNotFoundException, IOException, SQLException, JRException {

        User user = (User) session.getAttribute(BeanNameResolver.getBeanName(USER_BEAN));
        try {
            String path = "";
            String dbInquestNo = "";
            final HashMap map = new HashMap();

            String s = request.getParameter("dbInquestNo");
            if ((("").equals(s)) || s == null) {
                dbInquestNo = session.getAttribute("dbInquestNo").toString();
            } else {
                dbInquestNo = s;
            }
            final String pdfName = "Report_on_UnIdentified_Dead_Body";
            final String uniqeid = dbInquestNo;
            final String deadBodyLangCd = deadbodybusinessDelegate.getDeadBodyLangCd(dbInquestNo);

            path = "/AdditionalPrintouts/UnIdentfiedDeadBody//" + ReportsConstants.UN_IDENTIFIED_DEAD_BODY;

            String subPath = null;
            subPath = request.getRealPath("/WEB-INF/reports/AdditionalPrintouts/UnIdentfiedDeadBody/") + "/";
            map.put("SUBREPORT_DIR", subPath);
            map.put("lang_cd", deadBodyLangCd);
            map.put("dbInquestNum", dbInquestNo);

            jasperReportsUtil.getJasperPdfviewerFromImg(path, map, request, response, pdfName, uniqeid);
        } catch (Exception e) {

            LOGGER.error("**********************************Exception from printUnIdentifiedDeadBody**************"
                    + e.getMessage());
        }
    }

    /**
     * Function for print Unnatural Death
     *
     * @param request current HTTP request
     * @param response current HTTP response
     * @param session current HTTP session
     * @throws ClassNotFoundException if any error occurs
     * @throws IOException if any error occurs
     * @throws SQLException if any error occurs
     * @throws JRException if any error occurs
     */
    @RequestMapping(value = "/printUnnaturalDeath.htm", method = RequestMethod.GET)
    public void printUnnaturalDeath(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws ClassNotFoundException, IOException, SQLException, JRException {

        User user = (User) session.getAttribute(BeanNameResolver.getBeanName(USER_BEAN));
        int langCdLogged = user.getLangCd();
        if (session.getAttribute("recordLongCd") != null) {
            String langCdRecord = (String) session.getAttribute("recordLongCd");
            if (langCdRecord != null && !"".equals(langCdRecord)) {
                user.setLangCd(Integer.parseInt(langCdRecord));
            }
        }
        try {
            String path = "";
            final HashMap map = new HashMap();
            final String dbInquestNo = session.getAttribute("dbInquestNo").toString();

            final String pdfName = "Report_on_Unnatural_Death";
            final String uniqeid = dbInquestNo;
            final String deadBodyLangCd = deadbodybusinessDelegate.getDeadBodyLangCd(dbInquestNo);
            path = "/AdditionalPrintouts/UnnaturalDeath//" + ReportsConstants.UN_NATURAL_DEATH;

            String subPath = null;
            subPath = request.getRealPath("/WEB-INF/reports/AdditionalPrintouts/UnnaturalDeath/") + "/";
            map.put("SUBREPORT_DIR", subPath);
            map.put("lang_cd", deadBodyLangCd);
            map.put("dbInquestNum", dbInquestNo);

            jasperReportsUtil.getJasperPdfviewerFromImg(path, map, request, response, pdfName, uniqeid);
            user.setLangCd(langCdLogged);
        } catch (Exception e) {

            LOGGER.error("**********************************Exception from printUnnaturalDeath**************"
                    + e.getMessage());
        }
    }

    /**
     * method to fetch file uploaded during repost mortem
     *
     * @param request
     * @param response
     * @param session
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/historyFiles.htm", method = RequestMethod.GET)
    protected ModelAndView getHistoryFileDetails(HttpServletRequest request,
            HttpServletResponse response, HttpSession session) throws ApplicationException {
        User user = (User) session.getAttribute(BeanNameResolver.getBeanName(USER_BEAN));
        Long fileNo = Long.parseLong(request.getParameter("fileNum"));
        LOGGER.info("file NO isssss" + fileNo);
        FileUploadDownloadBean file1 = new FileUploadDownloadBean();
        file1 = deadbodybusinessDelegate.getHistoryFiles(fileNo, user);
        downloadFiles(file1, response);
        return null;
    }

    /**
     * method to download files
     *
     * @param dbFile
     * @param response
     * @throws Exception
     */
    private void downloadFiles(FileUploadDownloadBean dbFile, HttpServletResponse response) throws ApplicationException {
        try {
            String fileName = dbFile.getFileName();
            String fileType = fileName.substring(fileName.indexOf(".") + 1, fileName.length());

            if (("txt").equalsIgnoreCase(fileType.trim())) {
                response.setContentType("text/plain");
            } else if (("doc").equalsIgnoreCase(fileType.trim())) {
                response.setContentType("application/msword");
            } else if (("xls").equalsIgnoreCase(fileType.trim())) {
                response.setContentType("application/vnd.ms-excel");
            } else if (("pdf").equalsIgnoreCase(fileType.trim())) {
                response.setContentType("application/pdf");
            } else if (("ppt").equalsIgnoreCase(fileType.trim())) {
                response.setContentType("application/ppt");
            } else {
                response.setContentType("application/octet-stream");
            }
            response.setHeader("Content-Disposition", "attachment; filename=\"" + dbFile.getFileName() + "\"");
            response.setHeader("cache-control", "no-cache");
            byte[] fileBytes = dbFile.getFileBytes();
            ServletOutputStream outs = response.getOutputStream();
            outs.write(fileBytes);
            outs.flush();
            outs.close();
        } catch (Exception e) {
            LOGGER.error("Error in downloadig file" + e.getMessage());
        }
    }

    /**
     * Function for printing PostMortem Request Report
     *
     * @param request current HTTP request
     * @param response current HTTP response
     * @param session current HTTP session
     * @throws ClassNotFoundException if any error occurs
     * @throws IOException if any error occurs
     * @throws SQLException if any error occurs
     * @throws JRException if any error occurs
     */
    @RequestMapping(value = "/printpostmortemrequesthistory.htm", method = RequestMethod.GET)
    public void printPostMortemRequestHistory(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws ClassNotFoundException, IOException, SQLException, JRException {

        User user = (User) session.getAttribute(BeanNameResolver.getBeanName(USER_BEAN));
        try {
            String path = "";
            final HashMap map = new HashMap();
            final String inquestNumber = request.getParameter("inQuestNumber");
            final String dbPmSrNo = request.getParameter("dbPmSrNo");
            final String pdfName = "PostMortemRequestReportHistory";
            final String uniqeid = inquestNumber;

            path = "/AdditionalPrintouts/PostMortemRequestReportHistory//" + ReportsConstants.POSTMORTEM_REQUEST_REPORT_HISTORY;

            String subPath = null;
            subPath = request.getRealPath("/WEB-INF/reports/AdditionalPrintouts/PostMortemRequestReportHistory/") + "/";
            map.put("SUBREPORT_DIR", subPath);
            map.put("Langcd", user.getLangCd().toString());
            map.put("InquestNo", inquestNumber);
            map.put("DbPmSrNo", dbPmSrNo);

            jasperReportsUtil.getJasperPdfviewerFromImg(path, map, request, response, pdfName, uniqeid);
        } catch (Exception e) {

            LOGGER.error("**********************************Exception from printPostmortemRequest**************"
                    + e.getMessage());
        }
    }

    /**
     * Function for printing PostMortem Request Report
     *
     * @param request current HTTP request
     * @param response current HTTP response
     * @param session current HTTP session
     * @throws ClassNotFoundException if any error occurs
     * @throws IOException if any error occurs
     * @throws SQLException if any error occurs
     * @throws JRException if any error occurs
     */
    @RequestMapping(value = "/printpostmortemexaminationhistory.htm", method = RequestMethod.GET)
    public void printpostmortemreporthistory(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws ClassNotFoundException, IOException, SQLException, JRException {

        User user = (User) session.getAttribute(BeanNameResolver.getBeanName(USER_BEAN));
        try {
            String path = "";
            final HashMap map = new HashMap();
            final String inquestNumber = request.getParameter("inQuestNumber");
            final String dbPmSrNo = request.getParameter("dbPmSrNo");
            final String pdfName = "PostMortemExaminationHistory";
            final String uniqeid = inquestNumber;

            path = "/AdditionalPrintouts/PostmortenExaminationHistory//" + ReportsConstants.POSTMORTEM_EXAMINATION_REPORT_HISTORY;

            String subPath = null;
            subPath = request.getRealPath("/WEB-INF/reports/AdditionalPrintouts/PostmortenExaminationHistory/") + "/";
            map.put("SUBREPORT_DIR", subPath);
            map.put("Langcd", user.getLangCd().toString());
            map.put("InquestNo", inquestNumber);
            map.put("DbPmSrNo", dbPmSrNo);

            jasperReportsUtil.getJasperPdfviewerFromImg(path, map, request, response, pdfName, uniqeid);
        } catch (Exception e) {

            LOGGER.error("**********************************Exception from printpostmortemexaminationhistory**************"
                    + e.getMessage());
        }
    }

    /**
     * Function for printing RePostMortem Request Report
     *
     * @param request current HTTP request
     * @param response current HTTP response
     * @param session current HTTP session
     * @throws ClassNotFoundException if any error occurs
     * @throws IOException if any error occurs
     * @throws SQLException if any error occurs
     * @throws JRException if any error occurs
     */
    /*
     * @RequestMapping(value = "/printDeadBodyRePostMortemRequestReport.htm",
     * method = RequestMethod.GET) public void
     * printDeadBodyRePostMortemRequestReport(HttpServletRequest request,
     * HttpServletResponse response, HttpSession session) throws
     * ClassNotFoundException, IOException, SQLException, JRException {
     *
     * User user = (User)
     * session.getAttribute(BeanNameResolver.getBeanName(USER_BEAN)); try {
     * String path = ""; final HashMap map = new HashMap(); final String
     * inquestNumber = request.getParameter("inqNoToDisp").toString();
     *
     * final String pdfName = "RePostMortemRequestReport"; final String uniqeid
     * = inquestNumber;
     *
     * path = "/AdditionalPrintouts/DeadBodyRePostMortemRequestReport//" +
     * ReportsConstants.DEAD_BODY_REPOST_MORTEM_REQUEST_REPORT;
     *
     * String subPath = null; subPath =
     * request.getRealPath("/WEB-INF/reports/AdditionalPrintouts/DeadBodyRePostMortemRequestReport/")
     * + "/"; map.put("SUBREPORT_DIR", subPath); map.put("Lang_cd",
     * user.getLangCd().toString()); map.put("Inquest_num", inquestNumber);
     *
     * jasperReportsUtil.getJasperPdfviewerFromImg(path, map, request, response,
     * pdfName, uniqeid); } catch (Exception e) {
     *
     * LOGGER.error("**********************************Exception from
     * printDeadBodyRePostMortemRequestReport**************" + e.getMessage());
     * } }
     */
    /**
     * Function for printing PostMortem Request Report
     *
     * @param request current HTTP request
     * @param response current HTTP response
     * @param session current HTTP session
     * @throws ClassNotFoundException if any error occurs
     * @throws IOException if any error occurs
     * @throws SQLException if any error occurs
     * @throws JRException if any error occurs
     */
    @RequestMapping(value = "/printRePostmortemExaminationReport.htm", method = RequestMethod.GET)
    public void printRePostmortemExaminationReport(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws ClassNotFoundException, IOException, SQLException, JRException {

        User user = (User) session.getAttribute(BeanNameResolver.getBeanName(USER_BEAN));
        try {
            String path = "";
            final HashMap map = new HashMap();
            final String inquestNumber = session.getAttribute(CommonConstants.FORM_PRINT_KEY).toString();

            final String pdfName = "RePostMartemExamReport";
            final String uniqeid = inquestNumber;

            path = "/AdditionalPrintouts/DeadBodyRepostMortemExaminationReport//" + ReportsConstants.DEAD_BODY_REPOST_MORTEM_EXAMINATION_REPORT;

            String subPath = null;
            subPath = request.getRealPath("/WEB-INF/reports/AdditionalPrintouts/DeadBodyRepostMortemExaminationReport/") + "/";
            map.put("SUBREPORT_DIR", subPath);
            map.put("Lang_cd", user.getLangCd().toString());
            map.put("Inquest_num", inquestNumber);

            jasperReportsUtil.getJasperPdfviewerFromImg(path, map, request, response, pdfName, uniqeid);
        } catch (Exception e) {

            LOGGER.error("**********************************Exception from printInquestOfDeadBody**************"
                    + e.getMessage());
        }
    }

    /**
     * Function for printing PostMortem Request Report
     *
     * @param request current HTTP request
     * @param response current HTTP response
     * @param session current HTTP session
     * @throws ClassNotFoundException if any error occurs
     * @throws IOException if any error occurs
     * @throws SQLException if any error occurs
     * @throws JRException if any error occurs
     */
    @RequestMapping(value = "/printDeadBodyRePostMortemRequestReport.htm", method = RequestMethod.GET)
    public void printRePostmorteprintDeadBodyRePostMortemRequestReportmReport(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws ClassNotFoundException, IOException, SQLException, JRException {

        User user = (User) session.getAttribute(BeanNameResolver.getBeanName(USER_BEAN));
        try {
            String path = "";
            final HashMap map = new HashMap();
            final String inquestNumber = session.getAttribute(CommonConstants.FORM_PRINT_KEY).toString();

            final String pdfName = "PostMortemRequestReport";
            final String uniqeid = inquestNumber;

            path = "/AdditionalPrintouts/DeadBodyRePostMortemRequestReport//" + ReportsConstants.DEAD_BODY_REPOST_MORTEM_REQUEST_REPORT;

            String subPath = null;
            subPath = request.getRealPath("/WEB-INF/reports/AdditionalPrintouts/DeadBodyRePostMortemRequestReport/") + "/";
            map.put("SUBREPORT_DIR", subPath);
            map.put("Langcd", user.getLangCd().toString());
            map.put("InquestNo", inquestNumber);

            jasperReportsUtil.getJasperPdfviewerFromImg(path, map, request, response, pdfName, uniqeid);
        } catch (Exception e) {

            LOGGER.error("**********************************Exception from printPostmortemRequest**************"
                    + e.getMessage());
        }
    }

    @RequestMapping(value = "/viewunidentifieddeadbodytransfer.htm", method = RequestMethod.GET)
    public ModelAndView viewDeadbodyTransferGrid(@ModelAttribute("regDeadbodyFormBean") TDeadBodyRegistrationFormBean regDeadbodyFormBean, final ModelMap model, final HttpServletRequest request, final HttpSession session) {
        try {
            LOGGER.debug("Preparing to load search page for uidb");
            session.removeAttribute(RegistrationConstants.DEADBODY_WITNESS_INFO_BEAN_ID);
        } catch (Exception e) {
            LOGGER.error("**********************************Exception from printPostmortemRequest**************"
                    + e.getMessage());
        }
        return new ModelAndView("viewunidentifieddeadbodygrid", model);
    }

    @RequestMapping(value = "transferunidentifieddedbdy.htm", method = RequestMethod.POST)
    public ModelAndView getDeadBodyTransferView(@ModelAttribute("regDeadbodyFormBean") TDeadBodyRegistrationFormBean regDeadbodyFormBean,
            ModelMap model, HttpSession session, HttpServletRequest request, HttpServletResponse response) {
        final User user = (User) session.getAttribute(BeanNameResolver.getBeanName("USER_BEAN"));
        String inquestNum = request.getParameter("inquestNum");
        String inquestDt = request.getParameter("inquestDt");
        String psName = request.getParameter("psName");
        String identstatus = request.getParameter("identstatus");
        String deadBodyPsCode = request.getParameter("deadBodyPsCode");

        String oldDistrict = deadbodybusinessDelegate.getOldDistrict(deadBodyPsCode, user);
        final LinkedHashMap<Integer, String> statesMap = new LinkedHashMap<Integer, String>();
        statesMap.put(user.getStateCd(), user.getState());
        String inquestNumDisplay = inquestNum.substring(inquestNum.length() - 4, inquestNum.length()) + "/" + inquestDt.substring(6, 10);
        int submitAllowedOrNotFlag = deadbodybusinessDelegate.getSubmitAllowedOrNot(inquestNum, user);
        if (submitAllowedOrNotFlag != 0) {
            model.addAttribute("submitAllowedOrNot", true);
            model.addAttribute("submitHidden", "hidden");
        } else {
            model.addAttribute("submitAllowedOrNot", false);
            model.addAttribute("submitHidden", "");
            regDeadbodyFormBean.setFromPsName(user.getPoliceStation());
        }
        model.addAttribute("statesMap", statesMap);
        model.addAttribute("identstatus", identstatus);
        model.addAttribute("inquestNumDisplay", inquestNumDisplay);
        model.addAttribute("inquestNum", inquestNum);
        model.addAttribute("inquestDt", inquestDt);
        model.addAttribute("psName", psName);
        model.addAttribute("oldPsCd", user.getPoliceStationCd());
        model.addAttribute("oldDistrict", oldDistrict);
        return new ModelAndView("deadbodytransferview", model);
    }

    @RequestMapping(value = "transferuidb.htm", method = RequestMethod.POST)
    public ModelAndView saveTransferUidb(@ModelAttribute("regDeadbodyFormBean") TDeadBodyRegistrationFormBean regDeadbodyFormBean,
            ModelMap modelMap, HttpSession session) {
        DelegateWrapper wrapper = new DelegateWrapper();
        ErrorList errorList = null;
        ModelAndView modelView = null;
        final User user = (User) session.getAttribute(BeanNameResolver.getBeanName(USER_BEAN));
        try {
            regDeadbodyFormBean.setLangCd(user.getLangCd());
            regDeadbodyFormBean.setRecordCreatedBy(user.getPsStaffCd());
            regDeadbodyFormBean.setRecordUpdatedby(user.getPsStaffCd());
            String inquestNumDisplay = regDeadbodyFormBean.getInquestNum();
            String inquestDt = regDeadbodyFormBean.getInquestDt();
            inquestNumDisplay = inquestNumDisplay.substring(11, 15) + "/" + inquestDt.substring(6, 10);
            wrapper.addFormBean(RegistrationConstants.UIDB_TRANSFER_FORM_BEAN_ID, regDeadbodyFormBean);
            errorList = new ErrorList();
            deadbodybusinessDelegate.transferUidb(wrapper, user, errorList);
            String message = null;
            if (errorList.hasErrors()) {
                for (String errorCode : errorList.getErrors()) {
                    message = MessageSelector.getMessage(errorCode);
                    modelMap.addAttribute("errorMsg", true);
                    modelMap.addAttribute("message", message);
                    modelView = new ModelAndView("deadbodytransferview");
                }
            } else {
                modelMap.clear();
                message = ResourceUtil.getCommonMessage("MSG_7000", user.getLangCd());
                session.setAttribute(CommonConstants.MESSAGE_CODE_SUCCESS, true);
                session.setAttribute(CommonConstants.MESSAGE_NUM, inquestNumDisplay);
                modelView = new ModelAndView(new RedirectView("viewunidentifieddeadbodytransfer.htm"));
            }
        } catch (Exception e) {
            LOG.error("Error in transfer UIDB {}", e.getMessage());
        } finally {
            wrapper = null;
            errorList = null;
        }
        return modelView;
    }

    @RequestMapping(value = "/viewreregistrationoftransfereduidb.htm", method = RequestMethod.GET)
    public ModelAndView viewDeadbodyReregistrationGrid(@ModelAttribute("regdeadbody") TDeadBodyRegistrationFormBean regDeadbodyFormBean, final ModelMap model, final HttpServletRequest request, final HttpSession session) {
        try {
            LOGGER.debug("Preparing to load search page for uidb");
            session.removeAttribute(RegistrationConstants.DEADBODY_WITNESS_INFO_BEAN_ID);
        } catch (Exception e) {
            LOGGER.error("**********************************Exception from printPostmortemRequest**************"
                    + e.getMessage());
        }
        return new ModelAndView("viewreregistrationoftransfereduidb", model);
    }

    @RequestMapping(value = "/reregisteruidbview.htm", method = RequestMethod.POST)
    protected ModelAndView reRegistrationUIDB(@ModelAttribute("regdeadbody") TDeadBodyRegistrationFormBean regDeadbodyFormBean,
            BindingResult result, final HttpServletRequest request, final HttpSession session, ModelMap model, final ErrorList errorList) throws ApplicationException {
        User user = (User) session.getAttribute(BeanNameResolver.getBeanName(USER_BEAN));
        regDeadbodyFormBean.setInquestNumber(request.getParameter("inquestNumber"));
        clearSessionAttributesDeadBody(session);
        session.setAttribute("inquestNo", regDeadbodyFormBean.getInquestNumber());
        String deadbodyStatus = request.getParameter("status");
        regDeadbodyFormBean.setStatus(deadbodyStatus);
        int langcd = 0;
        if (request.getParameter("langcd") != null) {
            langcd = Integer.parseInt(request.getParameter("langcd"));
            if (langcd != 0) {
                user.setSearchlangcd(langcd);
            }
        }
        try {
            if ((regDeadbodyFormBean.getInquestNumber() != null) && (!regDeadbodyFormBean.getInquestNumber().equals(""))) {
                regDeadbodyFormBean = deadbodybusinessDelegate.deadbodyViewForReRegistration(regDeadbodyFormBean.getInquestNumber(), deadbodyStatus, user);
            }
            regDeadbodyFormBean.setRegState(user.getStateWithLang());
            regDeadbodyFormBean.setRegDistrict(user.getDistrictWithLang());
            regDeadbodyFormBean.setRegPs(user.getPsWithLang());
            regDeadbodyFormBean.setInformerDistrictCd(user.getDistrictCd().toString());
            regDeadbodyFormBean.setInformerPsCd(user.getPoliceStationCd().toString());
            regDeadbodyFormBean.setInformerStateCd(user.getStateCd().toString());
            regDeadbodyFormBean.setInformerPermDistrictEng(user.getDistrictCd().toString());
            regDeadbodyFormBean.setInformerPermPsEng(user.getPoliceStationCd().toString());
            regDeadbodyFormBean.setInformerPermStateEng(user.getStateCd().toString());
            regDeadbodyFormBean.setDeceasedDistrictEng(user.getDistrictCd().toString());
            regDeadbodyFormBean.setDeceasedPsEng(user.getPoliceStationCd().toString());
            regDeadbodyFormBean.setDeceasedStateEng(user.getStateCd().toString());
            regDeadbodyFormBean.setDeceasedPermDistrictEng(user.getDistrictCd().toString());
            regDeadbodyFormBean.setDeceasedPermPsEng(user.getPoliceStationCd().toString());
            regDeadbodyFormBean.setDeceasedPermStateEng(user.getStateCd().toString());
            regDeadbodyFormBean.setIdentifierDistrictEng(user.getDistrictCd().toString());
            regDeadbodyFormBean.setIdentifierPsEng(user.getPoliceStationCd().toString());
            regDeadbodyFormBean.setIdentifierStateEng(user.getStateCd().toString());
            regDeadbodyFormBean.setIdentifierPermDistrictEng(user.getDistrictCd().toString());
            regDeadbodyFormBean.setIdentifierPermPsEng(user.getPoliceStationCd().toString());
            regDeadbodyFormBean.setIdentifierPermStateEng(user.getStateCd().toString());
            populateSelectionFieldsData(model, session);
            regDeadbodyFormBean.setOriginalRecord(1);
            String hiddenDeathDate = regDeadbodyFormBean.getDeathDt();
            model.addAttribute("hiddenDeathDate", hiddenDeathDate);
            model.addAttribute("regdeadbody", regDeadbodyFormBean);
            model.addAttribute("user", user);

        } finally {
            regDeadbodyFormBean = null;
        }
        return new ModelAndView("reregdeadbody", model);
    }

    @RequestMapping(value = "/deadBodyEnquiryFileRemove.htm", method = RequestMethod.POST)
    public ModelAndView removeEnquiryFiles(HttpServletRequest request,
            HttpServletResponse response, HttpSession session) throws ApplicationException {
        Long enquiryFileSrNo = Long.parseLong(request.getParameter("enquiryFileSrNo"));

        List enquiryFileRemoveList = (List) session.getAttribute("dbEnquiryFileRemoveListSession");
        if (enquiryFileRemoveList == null) {
            enquiryFileRemoveList = new ArrayList();
        }
        enquiryFileRemoveList.add(enquiryFileSrNo);
        session.setAttribute("dbEnquiryFileRemoveListSession", enquiryFileRemoveList);
        final Map<String, Object> modelMap = new HashMap<String, Object>();
        modelMap.put("rows", enquiryFileRemoveList);
        return new ModelAndView("jsonView", modelMap);

    }

    /**
     * Get existing UIDB
     *
     * @param model ModelMap
     * @param request HttpServletRequest
     * @param session HttpSession
     * @return model
     */
    @RequestMapping(value = "/existinguidbpopup.htm", method = RequestMethod.GET)
    public ModelAndView getExistingUidb(@ModelAttribute("existinguidbpopup") TDeadBodyRegistrationFormBean regDeadbodyFormBean, final ModelMap model, final HttpServletRequest request, final HttpSession session) {

        return new ModelAndView("existinguidbpopup", model);
    }

    /**
     * Function for saving already known UIDB
     *
     * @param model
     * @param request
     * @param response
     * @param session
     * @return
     * @throws ApplicationException
     */
    @RequestMapping(value = "/settingExistingUidbToBean.htm", method = RequestMethod.POST)
    public ModelAndView settingExistingUidbToBean(ModelMap model, HttpServletRequest request, HttpServletResponse response, HttpSession session) throws ApplicationException {
        User user = (User) session.getAttribute(BeanNameResolver.getBeanName(USER_BEAN));
        String displaySuccessMsg = "";
        session.removeAttribute("existingInquestNo");
        final String inquestNoFromExisting = request.getParameter("inquestNoFromExisting");
        session.setAttribute("existingInquestNo", inquestNoFromExisting);
        displaySuccessMsg = RegistrationConstants.MSG_SUCCESS;
        return new ModelAndView(new RedirectView("existinguidbpopup.htm?displaySuccessMsg=" + displaySuccessMsg));

    }

    /**
     * Function for saving already known UIDB
     *
     * @param model
     * @param request
     * @param response
     * @param session
     * @return
     * @throws ApplicationException
     */
    @RequestMapping(value = "/redirecttomainpage.htm", method = RequestMethod.POST)
    public ModelAndView redirectToMainPage(ModelMap model, HttpServletRequest request, HttpServletResponse response, HttpSession session) throws ApplicationException {
        User user = (User) session.getAttribute(BeanNameResolver.getBeanName(USER_BEAN));
        TDeadBodyRegistrationFormBean regDeadbodyFormBean = new TDeadBodyRegistrationFormBean();
        if (session.getAttribute("existingInquestNo") != null) {
            final String inquestNoFromExisting = session.getAttribute("existingInquestNo").toString();
            regDeadbodyFormBean = deadbodybusinessDelegate.getExistingUidbDetails(inquestNoFromExisting, user);
            final Object[] addAccusedInfoDetail = {
                //General Information Tab
                regDeadbodyFormBean.getInquestDoneByCd(), //0
                regDeadbodyFormBean.getFoundPlc(), //1
                regDeadbodyFormBean.getInquestBy(), //2
                regDeadbodyFormBean.getGdNum(), //3
                regDeadbodyFormBean.getGdDt(), //4
                regDeadbodyFormBean.getInformationSrcCd(),//5
                regDeadbodyFormBean.getInformationSrc(), //6
                regDeadbodyFormBean.getInformationMode(), //7
                regDeadbodyFormBean.getLinkedFirNum(), //8
                regDeadbodyFormBean.getInformRecvDt(), //9
                regDeadbodyFormBean.getGenOtherInformation(), //10
                regDeadbodyFormBean.getDbFoundDistancePs(), //11
                regDeadbodyFormBean.getDbFoundDt(), //12
                regDeadbodyFormBean.getDbFoundDirectionPs(), //13
                regDeadbodyFormBean.getIsPmWaiverRequest(), //14
                regDeadbodyFormBean.getApprvOfficer(), //15
                regDeadbodyFormBean.getWaiverDetails(), //16
                //Informant Information
                regDeadbodyFormBean.getInformerUid(), //17
                regDeadbodyFormBean.getInformerFirstName(), //18
                regDeadbodyFormBean.getInformerMiddleName(), //19
                regDeadbodyFormBean.getInformerLastName(), //20
                regDeadbodyFormBean.getInformerRelationTypeCd(), //21
                regDeadbodyFormBean.getInformerMobile1(), //22
                regDeadbodyFormBean.getInformerMobile2(), //23
                regDeadbodyFormBean.getInformerTelephone1(), //24
                regDeadbodyFormBean.getInformerTelephone2(), //25
                regDeadbodyFormBean.getInformerTelephone3(), //26
                regDeadbodyFormBean.getInformerPermAddressLine1(), //27
                regDeadbodyFormBean.getInformerPermAddressLine2(), //28
                regDeadbodyFormBean.getInformerPermAddressLine3(), //29
                regDeadbodyFormBean.getInformerPermVillage(), //30
                regDeadbodyFormBean.getInformerPermTehsil(), //31
                regDeadbodyFormBean.getInformerPermPincode(), //32
                regDeadbodyFormBean.getInformerAddressLine1(), //33
                regDeadbodyFormBean.getInformerAddressLine2(), //34
                regDeadbodyFormBean.getInformerAddressLine3(), //35
                regDeadbodyFormBean.getInformerVillage(), //36
                regDeadbodyFormBean.getInformerTehsil(), //37
                regDeadbodyFormBean.getInformerPincode(), //38
                //Identifier Details
                regDeadbodyFormBean.getIdentifierUid(),//39
                regDeadbodyFormBean.getIdentifierFirstName(), //40
                regDeadbodyFormBean.getIdentifierMiddleName(), //41
                regDeadbodyFormBean.getIdentifierLastName(), //42
                regDeadbodyFormBean.getIdentifierRelationTypeEng(), //43
                regDeadbodyFormBean.getIdentifierEmail(), //44
                regDeadbodyFormBean.getIdentifierRelativeName(), //45
                regDeadbodyFormBean.getIdentifierAddressLine1(), //46
                regDeadbodyFormBean.getIdentifierAddressLine2(), //47
                regDeadbodyFormBean.getIdentifierAddressLine3(), //48
                regDeadbodyFormBean.getIdentifierVillage(), //49
                regDeadbodyFormBean.getIdentifierTehsil(), //50
                regDeadbodyFormBean.getIdentifierPincode(), //51
                regDeadbodyFormBean.getRdb2(),//52
                regDeadbodyFormBean.getIdentifierPermAddressLine1(), //53
                regDeadbodyFormBean.getIdentifierPermAddressLine2(), //54
                regDeadbodyFormBean.getIdentifierPermAddressLine3(), //55
                regDeadbodyFormBean.getIdentifierPermVillage(), //56
                regDeadbodyFormBean.getIdentifierPermTehsil(), //57
                regDeadbodyFormBean.getIdentifierPermPincode(), //58
                //Informer
                regDeadbodyFormBean.getInformerEmail(),//59
                regDeadbodyFormBean.getInformerRelativeName(),//60
                regDeadbodyFormBean.getInformerPermNationalityEng(),//61
                regDeadbodyFormBean.getInformerPermStateEng(),//62
                regDeadbodyFormBean.getInformerPermDistrictEng(),//63
                regDeadbodyFormBean.getInformerPermPsEng(),//64
                regDeadbodyFormBean.getInformerNationalityCd(),//65
                regDeadbodyFormBean.getInformerStateCd(),//66
                regDeadbodyFormBean.getInformerDistrictCd(),//67
                regDeadbodyFormBean.getInformerPsCd(),//68
                regDeadbodyFormBean.getRdb(),//69
                //Indetifier
                regDeadbodyFormBean.getIdentifierNationalityEng(),//70
                regDeadbodyFormBean.getIdentifierStateEng(),//71
                regDeadbodyFormBean.getIdentifierDistrictEng(),//72
                regDeadbodyFormBean.getIdentifierPsEng(),//73
                regDeadbodyFormBean.getIdentifierPermNationalityEng(),//74
                regDeadbodyFormBean.getIdentifierPermStateEng(),//75
                regDeadbodyFormBean.getIdentifierPermDistrictEng(),//76
                regDeadbodyFormBean.getIdentifierPermPsEng(),//77
                regDeadbodyFormBean.getIdentifierMobile1(),//78
                regDeadbodyFormBean.getIdentifierMobile2(),//79
                regDeadbodyFormBean.getIdentifierTelephone1(),//80
                regDeadbodyFormBean.getIdentifierTelephone2(),//81
                regDeadbodyFormBean.getIdentifierTelephone3(),//82
                regDeadbodyFormBean.getRelationDp(), //83
                regDeadbodyFormBean.getNationalidtypeInfomer(),//84
                regDeadbodyFormBean.getGdDisplayNo(),//85

                // BUG 25920 : Import UIDB -  missed out fields in general tab : Fix Starts
                regDeadbodyFormBean.getCaseTypeCd(),//86
                regDeadbodyFormBean.getDeathDt(),//87
                regDeadbodyFormBean.getBodyTypeCd(),//88
                regDeadbodyFormBean.getIsDbIdentified(),//89
                regDeadbodyFormBean.getEnqOfficerPsStaffCd(),//90
            // BUG 25920 : Import UIDB -  missed out fields in general tab : Fix Ends
            };

            model.put("rows", addAccusedInfoDetail);
        }


        return new ModelAndView("jsonView", model);
        //  return  new ModelAndView(new RedirectView("regdeadbody.htm"));

    }

    /**
     * Function for printing PostMortem Request Report
     *
     * @param request current HTTP request
     * @param response current HTTP response
     * @param session current HTTP session
     * @throws ClassNotFoundException if any error occurs
     * @throws IOException if any error occurs
     * @throws SQLException if any error occurs
     * @throws JRException if any error occurs
     */
    @RequestMapping(value = "/previousEnquiryPdfs.htm", method = RequestMethod.GET)
    public void printAllPreviousEnquiry(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws ClassNotFoundException, IOException, SQLException, JRException {
        List<String> outputFilepathList = new ArrayList<String>();
        String outputFilepath = null;
        String path = null;
        User user = (User) session.getAttribute(BeanNameResolver.getBeanName(USER_BEAN));
        try {

            String previousInqstNum = request.getParameter("previousInqstNum");
            String previousPanchnamaDone = request.getParameter("previousPanchnamaDone");
            String previousPmRequestDone = request.getParameter("previousPmRequestDone");
            String previousPmReportDone = request.getParameter("previousPmReportDone");
            String rowCount = request.getParameter("rowCount");
            Integer rowCountInt = Integer.parseInt(rowCount);
            String previousRePmReportDone = request.getParameter("previousRePmReportDone");
            if (previousPanchnamaDone != null) {
                path = printDbPanchnama(request, response, session, previousInqstNum, CommonConstants.CONST_SUBMIT);
                outputFilepathList.add(path);
            }
            if (previousPmRequestDone != null) {
                path = printPostmortemRequest(request, response, session, previousInqstNum, CommonConstants.CONST_SUBMIT);
                outputFilepathList.add(path);
            }
            if (previousPmReportDone != null) {
                path = printPostmortemReport(request, response, session, previousInqstNum, CommonConstants.CONST_SUBMIT);
                outputFilepathList.add(path);
                path = printPostmortemDescription(request, response, session, previousInqstNum, CommonConstants.CONST_SUBMIT);
                outputFilepathList.add(path);
            }
            if (rowCountInt > 1) {
                path = printRePostmortemRequest(request, response, session, previousInqstNum, CommonConstants.CONST_SUBMIT);
                outputFilepathList.add(path);
            }
            if (previousRePmReportDone != null) {
                path = printRePostmortemReport(request, response, session, previousInqstNum, CommonConstants.CONST_SUBMIT);
                outputFilepathList.add(path);
            }


            List<InputStream> listOutputFiles = new ArrayList<InputStream>();
            for (String newPath : outputFilepathList) {
                LOGGER.info("newPath=======>" + newPath);
                File file = new File(newPath);
                FileInputStream fis = new FileInputStream(file);
                listOutputFiles.add(fis);
            }
            outputFilepath = session.getServletContext().getRealPath("/WEB-INF/tempFiles/" + session.getId() + "/test.pdf");
            OutputStream output = new FileOutputStream(outputFilepath);
            FileConversionUtil.mergePDFs(listOutputFiles, output, true, outputFilepath, response, "UIDB");

        } catch (Exception e) {

            LOGGER.error("**********************************Exception from previous Enquiry**************"
                    + e.getMessage());
        }
    }

    /**
     * This method is used to print DeadBody Panchnama
     *
     * @param request
     * @param response
     * @param session
     * @throws ClassNotFoundException
     * @throws IOException
     * @throws SQLException
     * @throws JRException
     */
    private String printDbPanchnama(HttpServletRequest request, HttpServletResponse response, HttpSession session, String previousInqstNum, String preview) throws ClassNotFoundException, IOException, SQLException, JRException {
        User user = (User) session.getAttribute(BeanNameResolver.getBeanName("USER_BEAN"));
        final String path = "/AdditionalPrintouts/InquestOfDeadBody" + ReportsConstants.INQUEST_OF_DEAD_BODY;
        final HashMap map = new HashMap();

        final String subath = request.getRealPath("/WEB-INF/reports/AdditionalPrintouts/InquestOfDeadBody/") + "/";
        map.put("SUBREPORT_DIR", subath);
        map.put("Lang_cd", user.getLangCd().toString());
        map.put("InquestNumber", previousInqstNum);

        final String reportFirName = "Inquest_Of_DeadBody";
        String inputPath = jasperReportsUtil.getPdfForCommonUseCases(path, map, request, response, reportFirName, previousInqstNum, session, preview);
        return inputPath;
    }

    /**
     * This method is used to print Postmortem Request
     *
     * @param request
     * @param response
     * @param session
     * @throws ClassNotFoundException
     * @throws IOException
     * @throws SQLException
     * @throws JRException
     */
    private String printPostmortemRequest(HttpServletRequest request, HttpServletResponse response, HttpSession session, String previousInqstNum, String preview) throws ClassNotFoundException, IOException, SQLException, JRException {
        User user = (User) session.getAttribute(BeanNameResolver.getBeanName("USER_BEAN"));
        final String path = "/AdditionalPrintouts/postmortemRequestReport" + ReportsConstants.POSTMORTEM_REQUEST_REPORT;
        final HashMap map = new HashMap();

        final String subath = request.getRealPath("/WEB-INF/reports/AdditionalPrintouts/postmortemRequestReport/") + "/";
        map.put("SUBREPORT_DIR", subath);
        map.put("Langcd", user.getLangCd().toString());
        map.put("InquestNo", previousInqstNum);

        final String reportFirName = "PostMortemRequestReport";
        String inputPath = jasperReportsUtil.getPdfForCommonUseCases(path, map, request, response, reportFirName, previousInqstNum, session, preview);
        return inputPath;

    }

    /**
     * This method is used to print Postmortem Report
     *
     * @param request
     * @param response
     * @param session
     * @throws ClassNotFoundException
     * @throws IOException
     * @throws SQLException
     * @throws JRException
     */
    private String printPostmortemReport(HttpServletRequest request, HttpServletResponse response, HttpSession session, String previousInqstNum, String preview) throws ClassNotFoundException, IOException, SQLException, JRException {
        User user = (User) session.getAttribute(BeanNameResolver.getBeanName("USER_BEAN"));
        final String path = "/AdditionalPrintouts/PostMortemExaminationReport" + ReportsConstants.POSTMORTEM_EXAM_REPORT;
        final HashMap map = new HashMap();

        final String subath = request.getRealPath("/WEB-INF/reports/AdditionalPrintouts/PostMortemExaminationReport/") + "/";

        map.put("SUBREPORT_DIR", subath);
        map.put("Lang_cd", user.getLangCd().toString());
        map.put("Inquest_num", previousInqstNum);

        final String reportFirName = "PostMortemExaminationReport";
        String inputPath = jasperReportsUtil.getPdfForCommonUseCases(path, map, request, response, reportFirName, previousInqstNum, session, preview);
        return inputPath;

    }

    /**
     * This method is used to print Postmortem Description Report
     *
     * @param request
     * @param response
     * @param session
     * @throws ClassNotFoundException
     * @throws IOException
     * @throws SQLException
     * @throws JRException
     */
    private String printPostmortemDescription(HttpServletRequest request, HttpServletResponse response, HttpSession session, String previousInqstNum, String preview) throws ClassNotFoundException, IOException, SQLException, JRException {
        User user = (User) session.getAttribute(BeanNameResolver.getBeanName("USER_BEAN"));
        final String path = "/AdditionalPrintouts/postmortemReportDescription" + ReportsConstants.POSTMORTEM_REQUEST_DESCREPTION;
        final HashMap map = new HashMap();

        final String subath = request.getRealPath("/WEB-INF/reports/AdditionalPrintouts/postmortemReportDescription/") + "/";

        map.put("SUBREPORT_DIR", subath);
        map.put("Langcd", user.getLangCd().toString());
        map.put("InquestNo", previousInqstNum);

        final String reportFirName = "PostMortemDescriptionReport";
        String inputPath = jasperReportsUtil.getPdfForCommonUseCases(path, map, request, response, reportFirName, previousInqstNum, session, preview);
        return inputPath;

    }

    /**
     * This method is used to print RePostmortem Request
     *
     * @param request
     * @param response
     * @param session
     * @throws ClassNotFoundException
     * @throws IOException
     * @throws SQLException
     * @throws JRException
     */
    private String printRePostmortemRequest(HttpServletRequest request, HttpServletResponse response, HttpSession session, String previousInqstNum, String preview) throws ClassNotFoundException, IOException, SQLException, JRException {
        User user = (User) session.getAttribute(BeanNameResolver.getBeanName("USER_BEAN"));
        final String path = "/AdditionalPrintouts/DeadBodyRePostMortemRequestReport" + ReportsConstants.DEAD_BODY_REPOST_MORTEM_REQUEST_REPORT;
        final HashMap map = new HashMap();

        final String subath = request.getRealPath("/WEB-INF/reports/AdditionalPrintouts/DeadBodyRePostMortemRequestReport/") + "/";

        map.put("SUBREPORT_DIR", subath);
        map.put("Langcd", user.getLangCd().toString());
        map.put("InquestNo", previousInqstNum);

        final String reportFirName = "RePostMortemRequest";
        String inputPath = jasperReportsUtil.getPdfForCommonUseCases(path, map, request, response, reportFirName, previousInqstNum, session, preview);
        return inputPath;

    }

    /**
     * This method is used to print RePostmortem Request
     *
     * @param request
     * @param response
     * @param session
     * @throws ClassNotFoundException
     * @throws IOException
     * @throws SQLException
     * @throws JRException
     */
    private String printRePostmortemReport(HttpServletRequest request, HttpServletResponse response, HttpSession session, String previousInqstNum, String preview) throws ClassNotFoundException, IOException, SQLException, JRException {
        User user = (User) session.getAttribute(BeanNameResolver.getBeanName("USER_BEAN"));
        final String path = "/AdditionalPrintouts/DeadBodyRepostMortemExaminationReport" + ReportsConstants.DEAD_BODY_REPOST_MORTEM_EXAMINATION_REPORT;
        final HashMap map = new HashMap();

        final String subath = request.getRealPath("/WEB-INF/reports/AdditionalPrintouts/DeadBodyRepostMortemExaminationReport/") + "/";

        map.put("SUBREPORT_DIR", subath);
        map.put("Lang_cd", user.getLangCd().toString());
        map.put("Inquest_num", previousInqstNum);

        final String reportFirName = "RePostMortemExaminationReport";
        String inputPath = jasperReportsUtil.getPdfForCommonUseCases(path, map, request, response, reportFirName, previousInqstNum, session, preview);
        return inputPath;

    }

    @RequestMapping(value = "/printIdentifiedAndUnnaturalDeath.htm", method = RequestMethod.GET)
    public void printIdentifiedAndUnnaturalDeath(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws ClassNotFoundException, IOException, SQLException, JRException {
        User user = (User) session.getAttribute(BeanNameResolver.getBeanName(USER_BEAN));
        try {
            String path = "";
            String dbInquestNo = "";
            final HashMap map = new HashMap();

            String s = request.getParameter("dbInquestNo");
            if ((("").equals(s)) || s == null) {
                dbInquestNo = session.getAttribute("dbInquestNo").toString();
            } else {
                dbInquestNo = s;
            }
            final String pdfName = "Report_on_Identified_And_Natural_Death";
            final String uniqeid = dbInquestNo;
            final String deadBodyLangCd = deadbodybusinessDelegate.getDeadBodyLangCd(dbInquestNo);

            path = "/AdditionalPrintouts/IdentifiedAndNatural//" + ReportsConstants.UN_IDENTIFIED_DEAD_BODY;

            String subPath = null;
            subPath = request.getRealPath("/WEB-INF/reports/AdditionalPrintouts/IdentifiedAndNatural/") + "/";
            map.put("SUBREPORT_DIR", subPath);
            map.put("lang_cd", deadBodyLangCd);
            map.put("dbInquestNum", dbInquestNo);

            jasperReportsUtil.getJasperPdfviewerFromImg(path, map, request, response, pdfName, uniqeid);
        } catch (Exception e) {

            LOGGER.error("**********************************Exception from printUnIdentifiedDeadBody**************"
                    + e.getMessage());
        }
    }
}
