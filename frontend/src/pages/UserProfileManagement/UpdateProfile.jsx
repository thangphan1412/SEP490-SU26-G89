import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { Container, Card, Row, Col, Form, Button, Spinner, Stack, Alert } from "react-bootstrap";
import {
    IconDeviceFloppy,
    IconEdit,
    IconFileDescription,
    IconShieldCheck,
    IconBuilding,
    IconClock,
    IconInfoCircle,
    IconLock,
    IconMailForward
} from "@tabler/icons-react";

import { getMyProfile, updateMyProfile } from "../../services/userService/userApi.js";
import authenService from "../../services/userService/authenService.js";

function UpdateProfile({ onSaveProfile }) {
    const navigate = useNavigate();

    // --- STATE CHO PROFILE ---
    const [userProfile, setUserProfile] = useState({
        fullName: "", email: "", phoneNumber: "",
        role: "", department: "", userId: ""
    });
    const [isLoadingData, setIsLoadingData] = useState(true);
    const [isSubmittingProfile, setIsSubmittingProfile] = useState(false);

    // --- STATE CHO CHANGE PASSWORD VÀ OTP ---
    const [pwdMethod, setPwdMethod] = useState("old");
    const [pwdData, setPwdData] = useState({ oldPassword: "", newPassword: "", confirmPassword: "", otp: "" });
    const [otpSent, setOtpSent] = useState(false);
    const [isSubmittingPwd, setIsSubmittingPwd] = useState(false);

    // THÊM: STATE CHO ĐẾM NGƯỢC 60 GIÂY
    const [countdown, setCountdown] = useState(0);

    // Load dữ liệu
    useEffect(() => {
        const fetchCurrentProfile = async () => {
            try {
                const response = await getMyProfile();
                const data = response.data.data;
                setUserProfile({
                    fullName: `${data.firstName || ""} ${data.lastName || ""}`.trim(),
                    email: data.email || "",
                    phoneNumber: data.numberPhone || "",
                    role: data.role || "N/A",
                    department: data.departmentName || "N/A",
                    userId: data.userId || "N/A"
                });
            } catch (error) {
                console.error("Lỗi tải thông tin:", error);
                alert("Không thể tải thông tin profile của bạn!");
                navigate("/user-profile/view");
            } finally {
                setIsLoadingData(false);
            }
        };
        fetchCurrentProfile();
    }, [navigate]);

    // THÊM: USE-EFFECT XỬ LÝ ĐẾM NGƯỢC THỜI GIAN
    useEffect(() => {
        let timer;
        if (countdown > 0) {
            timer = setInterval(() => {
                setCountdown((prev) => prev - 1);
            }, 1000);
        }
        // Dọn dẹp interval khi component unmount hoặc khi countdown = 0
        return () => clearInterval(timer);
    }, [countdown]);

    // --- HANDLER CHO PROFILE ---
    const handleProfileChange = (event) => {
        const { name, value } = event.target;
        setUserProfile((prev) => ({ ...prev, [name]: value }));
    };

    const handleSubmitProfile = async (event) => {
        event.preventDefault();
        setIsSubmittingProfile(true);
        try {
            const nameParts = userProfile.fullName.trim().split(" ");
            const payload = {
                firstName: nameParts[0] || "",
                lastName: nameParts.slice(1).join(" ") || "",
                email: userProfile.email,
                numberPhone: userProfile.phoneNumber
            };

            await updateMyProfile(payload);
            if (onSaveProfile) onSaveProfile(userProfile);
            alert("Cập nhật thông tin cá nhân thành công!");
            navigate("/user-profile/view");

        } catch (error) {
            alert("Có lỗi xảy ra: " + (error.response?.data?.message || "Vui lòng thử lại!"));
        } finally {
            setIsSubmittingProfile(false);
        }
    };

    // --- HANDLER CHO ĐỔI MẬT KHẨU ---
    const handlePwdChange = (event) => {
        const { name, value } = event.target;
        setPwdData((prev) => ({ ...prev, [name]: value }));
    };

    // Gọi API Gửi OTP
    const handleSendOtp = async () => {
        try {
            setIsSubmittingPwd(true);
            await authenService.forgot({ email: userProfile.email });
            setOtpSent(true);

            // KÍCH HOẠT ĐẾM NGƯỢC 60 GIÂY SAU KHI GỬI THÀNH CÔNG
            setCountdown(60);

            alert("Mã OTP đã được gửi đến email: " + userProfile.email);
        } catch (error) {
            alert("Lỗi gửi OTP: " + (error.response?.data?.message || "Vui lòng thử lại"));
        } finally {
            setIsSubmittingPwd(false);
        }
    };

    // Gọi API Lưu Mật Khẩu
    const handleSubmitPassword = async (event) => {
        event.preventDefault();
        if (pwdData.newPassword !== pwdData.confirmPassword) {
            alert("Mật khẩu xác nhận không khớp!");
            return;
        }

        setIsSubmittingPwd(true);
        try {
            if (pwdMethod === "old") {
                await authenService.changePassword({
                    oldPassword: pwdData.oldPassword,
                    newPassword: pwdData.newPassword
                });
            } else {
                await authenService.reset({
                    email: userProfile.email,
                    otp: pwdData.otp,
                    newPassword: pwdData.newPassword,
                    newPasswordConfirm: pwdData.confirmPassword
                });
            }
            alert("Đổi mật khẩu thành công! Vui lòng sử dụng mật khẩu mới cho lần đăng nhập sau.");
            setPwdData({ oldPassword: "", newPassword: "", confirmPassword: "", otp: "" });
            setOtpSent(false);
            setCountdown(0); // Reset bộ đếm nếu đổi xong
        } catch (error) {
            console.error("Lỗi:", error);
            const responseData = error.response?.data;

            // 1. Lấy thông báo chung
            let alertMessage = responseData?.message || "Vui lòng thử lại!";

            // 2. Móc lỗi chi tiết (Nếu Backend có gửi kèm trong biến data)
            if (responseData?.data && typeof responseData.data === 'object') {
                // Lấy tất cả các câu chửi của Backend ghép thành nhiều dòng
                const detailedErrors = Object.values(responseData.data).join('\n- ');
                alertMessage += "\n\nChi tiết lỗi:\n- " + detailedErrors;
            }

            // 3. Hiển thị lên màn hình
            alert("Có lỗi xảy ra: " + alertMessage);
        } finally {
            setIsSubmittingPwd(false);
        }
    };

    return (
        <div className="bg-light min-vh-screen">
            <Container fluid="lg" className="mb-5">

                {/* Header Tổng */}
                <div className="d-flex justify-content-between align-items-center border-bottom p-4 bg-white mt-4 border shadow-sm rounded-top-4">
                    <div>
                        <h1 className="h3 mb-1 fw-bold text-dark">Update Settings</h1>
                        <p className="text-muted mb-0">Manage your profile and security settings.</p>
                    </div>
                    <Button variant="outline-secondary" className="fw-bold px-3" onClick={() => navigate("/user-profile/view")} disabled={isLoadingData}>
                        Back to Profile
                    </Button>
                </div>

                {isLoadingData ? (
                    <Card className="text-center py-5 shadow-sm rounded-bottom-4 border-top-0">
                        <Spinner animation="border" variant="primary" />
                        <p className="mt-3 text-muted">Loading data...</p>
                    </Card>
                ) : (
                    <>
                        {/* =========================================
                            FORM 1: CẬP NHẬT THÔNG TIN CÁ NHÂN
                        ============================================= */}
                        <Form onSubmit={handleSubmitProfile}>
                            <Card className="mb-4 border rounded-bottom-4 shadow-sm border-top-0">
                                <Card.Body className="p-4">
                                    <div className="d-flex align-items-center gap-2 border-bottom pb-3 mb-4">
                                        <div className="p-2 bg-primary bg-opacity-10 text-primary rounded-circle d-flex"><IconEdit size={22} /></div>
                                        <h2 className="h5 fw-bold mb-0 text-dark">Profile Information</h2>
                                    </div>

                                    <Row className="g-4">
                                        <Col md={3} className="text-center border-end d-flex flex-column align-items-center justify-content-center pb-4 pb-md-0">
                                            <div className="position-relative mb-3">
                                                <div className="bg-primary text-white rounded-circle d-flex align-items-center justify-content-center fw-bold shadow-sm" style={{ width: "100px", height: "100px", fontSize: "36px" }}>
                                                    {userProfile.fullName ? userProfile.fullName.split(' ').map(n => n[0]).join('').substring(0,2).toUpperCase() : 'U'}
                                                </div>
                                            </div>
                                            <Button size="sm" variant="outline-secondary" className="fw-semibold px-3">Change Avatar</Button>
                                        </Col>

                                        <Col md={9} className="px-4">
                                            <Row className="g-4">
                                                <Col md={6}>
                                                    <Form.Group>
                                                        <Form.Label className="small fw-bold text-secondary">Full Name <span className="text-danger">*</span></Form.Label>
                                                        <Form.Control name="fullName" type="text" value={userProfile.fullName} onChange={handleProfileChange} required disabled={isSubmittingProfile} className="py-2" />
                                                    </Form.Group>
                                                </Col>
                                                <Col md={6}>
                                                    <Form.Group>
                                                        <Form.Label className="small fw-bold text-secondary">Email <span className="text-danger">*</span></Form.Label>
                                                        <Form.Control name="email" type="email" value={userProfile.email} onChange={handleProfileChange} required disabled={isSubmittingProfile} className="py-2" />
                                                    </Form.Group>
                                                </Col>
                                                <Col md={6}>
                                                    <Form.Group>
                                                        <Form.Label className="small fw-bold text-secondary">Phone Number <span className="text-danger">*</span></Form.Label>
                                                        <Form.Control name="phoneNumber" type="tel" value={userProfile.phoneNumber} onChange={handleProfileChange} required disabled={isSubmittingProfile} className="py-2" />
                                                    </Form.Group>
                                                </Col>
                                                <Col md={6}>
                                                    <Form.Group>
                                                        <Form.Label className="small fw-bold text-secondary">User ID</Form.Label>
                                                        <Form.Control type="text" value={userProfile.userId} disabled className="py-2 bg-light text-muted" />
                                                    </Form.Group>
                                                </Col>
                                            </Row>

                                            <div className="d-flex justify-content-end mt-4">
                                                <Button type="submit" variant="primary" className="fw-bold px-4 d-flex align-items-center gap-2" disabled={isSubmittingProfile}>
                                                    {isSubmittingProfile ? <Spinner animation="border" size="sm" /> : <IconDeviceFloppy size={19} />}
                                                    {isSubmittingProfile ? "Saving..." : "Save Profile"}
                                                </Button>
                                            </div>
                                        </Col>
                                    </Row>
                                </Card.Body>
                            </Card>
                        </Form>

                        {/* =========================================
                            FORM 2: CẬP NHẬT MẬT KHẨU
                        ============================================= */}
                        <Form onSubmit={handleSubmitPassword}>
                            <Card className="mb-4 border rounded-4 shadow-sm">
                                <Card.Body className="p-4">
                                    <div className="d-flex align-items-center gap-2 border-bottom pb-3 mb-4">
                                        <div className="p-2 bg-warning bg-opacity-10 text-warning rounded-circle d-flex"><IconLock size={22} /></div>
                                        <div>
                                            <h2 className="h5 fw-bold mb-0 text-dark">Security & Password</h2>
                                            <p className="small text-muted mb-0">Choose a method to change your password securely.</p>
                                        </div>
                                    </div>

                                    <Row className="g-4">
                                        {/* Cột Trái: Chọn Phương thức */}
                                        <Col md={4} className="border-end px-3">
                                            <div className="p-3 bg-light rounded border">
                                                <h6 className="fw-bold text-secondary mb-3">Phương thức xác thực:</h6>

                                                <Form.Check
                                                    type="radio" id="method-old" name="pwdMethod"
                                                    label={<span className="fw-medium">Nhập mật khẩu cũ</span>}
                                                    className="mb-2"
                                                    checked={pwdMethod === "old"}
                                                    onChange={() => setPwdMethod("old")}
                                                />
                                                <Form.Check
                                                    type="radio" id="method-otp" name="pwdMethod"
                                                    label={<span className="fw-medium">Xác thực OTP qua Email</span>}
                                                    checked={pwdMethod === "otp"}
                                                    onChange={() => { setPwdMethod("otp"); setOtpSent(false); setCountdown(0); }}
                                                />
                                            </div>

                                            {/* Hiển thị nút Gửi OTP nếu chọn OTP */}
                                            {pwdMethod === "otp" && (
                                                <div className="mt-3 text-center">
                                                    <p className="small text-muted mb-2">Mã OTP sẽ được gửi đến:<br/><strong>{userProfile.email}</strong></p>
                                                    <Button
                                                        variant="outline-primary"
                                                        size="sm"
                                                        className="w-100 fw-bold d-flex justify-content-center align-items-center gap-2"
                                                        onClick={handleSendOtp}
                                                        // KHÓA NÚT NẾU ĐANG CALL API HOẶC NẾU ĐANG ĐẾM NGƯỢC
                                                        disabled={isSubmittingPwd || !userProfile.email || countdown > 0}
                                                    >
                                                        <IconMailForward size={16} />
                                                        {countdown > 0 ? `Thử lại sau (${countdown}s)` : (otpSent ? "Gửi lại mã OTP" : "Gửi mã OTP")}
                                                    </Button>
                                                </div>
                                            )}
                                        </Col>

                                        {/* Cột Phải: Các ô nhập liệu */}
                                        <Col md={8} className="px-4">
                                            <Row className="g-3">
                                                {pwdMethod === "old" && (
                                                    <Col md={12}>
                                                        <Form.Group>
                                                            <Form.Label className="small fw-bold text-secondary">Mật khẩu cũ <span className="text-danger">*</span></Form.Label>
                                                            <Form.Control name="oldPassword" type="password" value={pwdData.oldPassword} onChange={handlePwdChange} required={pwdMethod === "old"} disabled={isSubmittingPwd} className="py-2" placeholder="Nhập mật khẩu hiện tại" />
                                                        </Form.Group>
                                                    </Col>
                                                )}

                                                {pwdMethod === "otp" && (
                                                    <Col md={12}>
                                                        <Form.Group>
                                                            <Form.Label className="small fw-bold text-secondary">Mã xác thực OTP <span className="text-danger">*</span></Form.Label>
                                                            <Form.Control name="otp" type="text" value={pwdData.otp} onChange={handlePwdChange} required={pwdMethod === "otp"} disabled={!otpSent || isSubmittingPwd} className="py-2" placeholder="Nhập mã 6 số từ email" />
                                                            {!otpSent && <Form.Text className="text-danger">Vui lòng bấm nút Gửi mã OTP trước!</Form.Text>}
                                                        </Form.Group>
                                                    </Col>
                                                )}

                                                <Col md={6}>
                                                    <Form.Group>
                                                        <Form.Label className="small fw-bold text-secondary">Mật khẩu mới <span className="text-danger">*</span></Form.Label>
                                                        <Form.Control name="newPassword" type="password" value={pwdData.newPassword} onChange={handlePwdChange} required disabled={isSubmittingPwd || (pwdMethod === "otp" && !otpSent)} className="py-2" />
                                                    </Form.Group>
                                                </Col>
                                                <Col md={6}>
                                                    <Form.Group>
                                                        <Form.Label className="small fw-bold text-secondary">Xác nhận mật khẩu mới <span className="text-danger">*</span></Form.Label>
                                                        <Form.Control name="confirmPassword" type="password" value={pwdData.confirmPassword} onChange={handlePwdChange} required disabled={isSubmittingPwd || (pwdMethod === "otp" && !otpSent)} className="py-2" />
                                                    </Form.Group>
                                                </Col>
                                            </Row>

                                            <div className="d-flex justify-content-end mt-4">
                                                <Button type="submit" variant="warning" className="fw-bold px-4 d-flex align-items-center gap-2 text-dark" disabled={isSubmittingPwd || (pwdMethod === "otp" && !otpSent)}>
                                                    {isSubmittingPwd ? <Spinner animation="border" size="sm" /> : <IconLock size={19} />}
                                                    {isSubmittingPwd ? "Processing..." : "Change Password"}
                                                </Button>
                                            </div>
                                        </Col>
                                    </Row>
                                </Card.Body>
                            </Card>
                        </Form>

                    </>
                )}
            </Container>
        </div>
    );
}

export default UpdateProfile;